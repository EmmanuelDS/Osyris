package be.gim.commons.feature;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.filter.identity.FeatureIdImpl;
import org.geotools.xml.gml.WFSFeatureTypeTransformer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import be.gim.commons.date.ISO8601DateFormat;
import be.gim.commons.geometry.GeometryUtils;
import be.gim.commons.geometry.operation.SplitOp;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;

@Named
public class FeatureUtils {

	public static String generateId(SimpleFeatureType featureType) {
		return generateId(featureType.getTypeName());
	}

	public static String generateId(String name) {
		return name.replace(":", "_") + "."
				+ RandomStringUtils.randomNumeric(16);
	}

	public static FeatureCollection<SimpleFeatureType, SimpleFeature> explode(
			SimpleFeature feature, SimpleFeatureType featureType)
			throws InstantiationException, IllegalAccessException, IOException {

		Geometry geometry = (Geometry) feature.getDefaultGeometry();

		FeatureCollection<SimpleFeatureType, SimpleFeature> result = new MemoryFeatureList(
				featureType);

		if (!(geometry instanceof GeometryCollection)) {
			result.add(clone(feature, featureType));
		} else {
			for (int i = 0; i < geometry.getNumGeometries(); i++) {
				SimpleFeature newFeature = createFeature(featureType);
				newFeature.setDefaultGeometry(geometry.getGeometryN(i));
				result.add(newFeature);
			}
		}

		return result;
	}

	public static SimpleFeature merge(
			FeatureCollection<SimpleFeatureType, SimpleFeature> features,
			SimpleFeatureType featureType) throws InstantiationException,
			IllegalAccessException, IOException {

		return merge(features.iterator(), featureType);
	}

	public static SimpleFeature merge(Iterator<SimpleFeature> iterator,
			SimpleFeatureType featureType) throws InstantiationException,
			IllegalAccessException, IOException {

		Geometry geometry = GeometryUtils.merge(iterator);
		if (geometry != null) {
			return createFeature(featureType, geometry);
		} else {
			return null;
		}
	}

	public static FeatureCollection<SimpleFeatureType, SimpleFeature> split(
			SimpleFeature feature, SimpleFeatureType featureType,
			LineString splitLine) throws InstantiationException,
			IllegalAccessException, IOException {

		FeatureCollection<SimpleFeatureType, SimpleFeature> result = new MemoryFeatureList(
				featureType);

		SplitOp splitter = new SplitOp((Geometry) feature.getDefaultGeometry(),
				splitLine);
		Collection<Geometry> geometries = splitter.split();

		for (Geometry geometry : geometries) {
			SimpleFeature newFeature = createFeature(featureType);
			newFeature.setDefaultGeometry(geometry);
			result.add(newFeature);
		}

		return result;
	}

	public static void clone(SimpleFeature source, SimpleFeature target) {

		SimpleFeatureType sourceType = source.getFeatureType();
		SimpleFeatureType targetType = target.getFeatureType();

		CoordinateReferenceSystem sourceCRS = sourceType
				.getCoordinateReferenceSystem();
		CoordinateReferenceSystem targetCRS = targetType
				.getCoordinateReferenceSystem();

		for (AttributeDescriptor targetDescriptor : target.getFeatureType()
				.getAttributeDescriptors()) {
			AttributeDescriptor sourceDescriptor = sourceType
					.getDescriptor(targetDescriptor.getName());
			if (sourceDescriptor != null) {
				Class<?> type = sourceDescriptor.getType().getBinding();
				if (targetDescriptor.getType().getBinding()
						.isAssignableFrom(type)) {
					try {
						if (Geometry.class.isAssignableFrom(type)) {
							target.setAttribute(targetDescriptor.getName(),
									GeometryUtils.transform((Geometry) source
											.getAttribute(sourceDescriptor
													.getName()), sourceCRS,
											targetCRS));
						} else if (Envelope.class.isAssignableFrom(type)) {
							target.setAttribute(targetDescriptor.getName(),
									GeometryUtils.transform((Envelope) source
											.getAttribute(sourceDescriptor
													.getName()), sourceCRS,
											targetCRS));
						} else {
							// Ignore id attribute
							if (!targetDescriptor.getName().toString()
									.equals("id")) {
								target.setAttribute(targetDescriptor.getName(),
										source.getAttribute(sourceDescriptor
												.getName()));
							}
						}
					} catch (Exception e) {
					}
				}
			}
		}

		try {
			target.setDefaultGeometry(GeometryUtils.transform(
					(Geometry) source.getDefaultGeometry(), sourceCRS,
					targetCRS));
		} catch (Exception e) {
		}
	}

	public static void clone(
			FeatureCollection<SimpleFeatureType, SimpleFeature> source,
			FeatureCollection<SimpleFeatureType, SimpleFeature> target) {

		SimpleFeatureType targetType = target.getSchema();

		Iterator<SimpleFeature> i = source.iterator();
		while (i.hasNext()) {
			target.add(convert(i.next(), targetType));
		}
	}

	public static SimpleFeature convert(SimpleFeature source,
			SimpleFeatureType targetType) {

		SimpleFeatureType sourceType = source.getFeatureType();
		if (targetType.equals(sourceType)
				&& ObjectUtils.equals(
						targetType.getCoordinateReferenceSystem(),
						sourceType.getCoordinateReferenceSystem())) {
			return source;
		}

		return clone(source, targetType);
	}

	public static SimpleFeature clone(SimpleFeature source) {

		return clone(source, source.getFeatureType());
	}

	public static SimpleFeature clone(SimpleFeature source,
			SimpleFeatureType targetType) {

		SimpleFeature target = createFeature(source.getID(), targetType);
		clone(source, target);

		return target;
	}

	public static FeatureCollection<SimpleFeatureType, SimpleFeature> convert(
			FeatureCollection<SimpleFeatureType, SimpleFeature> source,
			SimpleFeatureType targetType) {

		SimpleFeatureType sourceType = source.getSchema();
		if (targetType.equals(sourceType)
				&& ObjectUtils.equals(
						targetType.getCoordinateReferenceSystem(),
						sourceType.getCoordinateReferenceSystem())) {
			return source;
		}

		return clone(source, targetType);
	}

	public static CoordinateReferenceSystem getCRS(SimpleFeature feature,
			CoordinateReferenceSystem _default) {

		CoordinateReferenceSystem crs = GeometryUtils.getCRS(
				(Geometry) feature.getDefaultGeometry(), null);
		if (crs != null) {
			return crs;
		}

		if (feature.getDefaultGeometryProperty().getDescriptor()
				.getCoordinateReferenceSystem() != null) {
			return feature.getDefaultGeometryProperty().getDescriptor()
					.getCoordinateReferenceSystem();
		}
		if (feature.getFeatureType().getCoordinateReferenceSystem() != null) {
			GeometryUtils.toSRS(feature.getFeatureType()
					.getCoordinateReferenceSystem());
		}

		return _default;
	}

	public static void setCRS(
			FeatureCollection<SimpleFeatureType, SimpleFeature> features,
			CoordinateReferenceSystem crs) {

		Iterator<SimpleFeature> i = features.iterator();
		while (i.hasNext()) {
			GeometryUtils.setCRS((Geometry) i.next().getDefaultGeometry(), crs);
		}
	}

	public static void setCRS(SimpleFeature feature,
			CoordinateReferenceSystem crs) {

		GeometryUtils.setCRS((Geometry) feature.getDefaultGeometry(), crs);
	}

	public static FeatureCollection<SimpleFeatureType, SimpleFeature> clone(
			FeatureCollection<SimpleFeatureType, SimpleFeature> source,
			SimpleFeatureType targetType) {

		FeatureCollection<SimpleFeatureType, SimpleFeature> target = createFeatureCollection(
				source.getID(), targetType);
		clone(source, target);

		return target;
	}

	public static SimpleFeatureType transform(SimpleFeatureType featureType,
			CoordinateReferenceSystem crs) throws SchemaException {

		if (crs != null
				&& !crs.equals(featureType.getCoordinateReferenceSystem())) {
			return WFSFeatureTypeTransformer.transform(featureType, crs);
		} else {
			return featureType;
		}
	}

	public static SimpleFeature transform(SimpleFeature feature,
			CoordinateReferenceSystem crs) throws SchemaException {

		return convert(feature, transform(feature.getFeatureType(), crs));
	}

	public static FeatureCollection<SimpleFeatureType, SimpleFeature> transform(
			FeatureCollection<SimpleFeatureType, SimpleFeature> features,
			CoordinateReferenceSystem crs) throws SchemaException {

		return convert(features, transform(features.getSchema(), crs));
	}

	@SuppressWarnings("unchecked")
	public static FeatureCollection<SimpleFeatureType, SimpleFeature> toFeatureCollection(
			Object collection) {

		if (collection instanceof Map) {
			List<SimpleFeature> features = new ArrayList<SimpleFeature>();

			for (Map.Entry<String, Object> entry : ((Map<String, Object>) collection)
					.entrySet()) {
				if ("featureMember".equals(entry.getKey())
						|| "featureMembers".equals(entry.getKey())) {
					Object value = entry.getValue();
					if (value instanceof Collection) {
						features.addAll((Collection<SimpleFeature>) value);
					} else if (value instanceof SimpleFeature) {
						features.add((SimpleFeature) value);
					}
				}
			}

			return createFeatureCollection(features);
		} else if (collection instanceof FeatureCollection) {
			return (FeatureCollection<SimpleFeatureType, SimpleFeature>) collection;
		} else if (collection instanceof SimpleFeature[]) {
			return createFeatureCollection((SimpleFeature[]) collection, null);
		} else if (collection instanceof Collection) {
			return createFeatureCollection(
					(Collection<SimpleFeature>) collection, null);
		} else if (collection instanceof SimpleFeature) {
			return createFeatureCollection((SimpleFeature) collection);
		}

		return null;
	}

	public static FeatureCollection<SimpleFeatureType, SimpleFeature> createFeatureCollection(
			SimpleFeatureType featureType) {

		return new DefaultFeatureCollection(null, featureType);
	}

	public static FeatureCollection<SimpleFeatureType, SimpleFeature> createFeatureCollection(
			String id, SimpleFeatureType featureType) {

		return new DefaultFeatureCollection(id, featureType);
	}

	public static FeatureCollection<SimpleFeatureType, SimpleFeature> createFeatureCollection(
			SimpleFeature feature) {

		return createFeatureCollection(feature, feature.getFeatureType());
	}

	public static FeatureCollection<SimpleFeatureType, SimpleFeature> createFeatureCollection(
			SimpleFeature feature, SimpleFeatureType featureType) {

		FeatureCollection<SimpleFeatureType, SimpleFeature> result = createFeatureCollection(featureType);

		result.add(feature);

		return result;
	}

	public static FeatureCollection<SimpleFeatureType, SimpleFeature> createFeatureCollection(
			SimpleFeature[] features, SimpleFeatureType featureType) {

		FeatureCollection<SimpleFeatureType, SimpleFeature> result = createFeatureCollection(featureType);

		for (SimpleFeature feature : features) {
			result.add(feature);
		}

		return result;
	}

	public static FeatureCollection<SimpleFeatureType, SimpleFeature> createFeatureCollection(
			Collection<SimpleFeature> features, SimpleFeatureType featureType) {

		FeatureCollection<SimpleFeatureType, SimpleFeature> result = createFeatureCollection(featureType);

		for (SimpleFeature feature : features) {
			result.add(feature);
		}

		return result;
	}

	public static FeatureCollection<SimpleFeatureType, SimpleFeature> createFeatureCollection(
			Collection<SimpleFeature> features) {

		FeatureCollection<SimpleFeatureType, SimpleFeature> result = createFeatureCollection(!features
				.isEmpty() ? features.iterator().next().getFeatureType() : null);

		for (SimpleFeature feature : features) {
			result.add(feature);
		}

		return result;
	}

	public static SimpleFeature createFeature(SimpleFeatureType featureType) {

		return new SimpleFeatureImpl(
				new Object[featureType.getAttributeCount()], featureType,
				new FeatureIdImpl(generateId(featureType)), false);
	}

	public static SimpleFeature createFeature(SimpleFeatureType featureType,
			Geometry geometry) {

		SimpleFeature feature = createFeature(featureType);
		feature.setDefaultGeometry(geometry);
		return feature;
	}

	public static SimpleFeature createFeature(String id,
			SimpleFeatureType featureType) {

		return new SimpleFeatureImpl(
				new Object[featureType.getAttributeCount()], featureType,
				new FeatureIdImpl(id), false);
	}

	public static List<String> getFeatureIds(
			FeatureCollection<SimpleFeatureType, SimpleFeature> features) {

		List<String> ids = new ArrayList<String>(features.size());

		Iterator<SimpleFeature> i = features.iterator();
		while (i.hasNext()) {
			SimpleFeature feature = i.next();
			ids.add(feature.getID());
		}

		return ids;
	}

	public static List<String> getFeatureNames(
			FeatureCollection<SimpleFeatureType, SimpleFeature> features) {

		List<String> names = new ArrayList<String>(features.size());

		Iterator<SimpleFeature> i = features.iterator();
		while (i.hasNext()) {
			SimpleFeature feature = i.next();
			String name = (String) feature.getAttribute("name");
			if (name != null) {
				names.add(name);
			}
		}

		return names;
	}

	public static Object getAttribute(SimpleFeature feature,
			String attributeName) {

		if (StringUtils.isEmpty(attributeName)) {
			return null;
		}

		return feature.getAttribute(attributeName);
	}

	public static String getStringAttribute(SimpleFeature feature,
			String attributeName) {

		if (StringUtils.isEmpty(attributeName)) {
			return null;
		}

		return ObjectUtils.toString(feature.getAttribute(attributeName));
	}

	public static Date getTimeAttribute(SimpleFeature feature,
			String attributeName) {

		if (StringUtils.isEmpty(attributeName)) {
			return null;
		}

		Object value = feature.getAttribute(attributeName);

		if (value instanceof Date) {
			return (Date) value;
		} else if (value instanceof Calendar) {
			return ((Calendar) value).getTime();
		} else if (value instanceof Number) {
			return new Date(((Number) value).longValue());
		} else if (value instanceof String) {
			try {
				return new ISO8601DateFormat().parse((String) value);
			} catch (ParseException e) {
				return null;
			}
		} else {
			return null;
		}
	}
}
