package be.gim.specto.ui.component;

import java.util.Collections;
import java.util.List;

import org.conscientia.api.model.ModelObject;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.filter.Filter;
import org.richfaces.cdk.annotations.Attribute;
import org.richfaces.cdk.annotations.JsfComponent;

import be.gim.commons.filter.FilterUtils;
import be.gim.specto.api.context.FeatureMapLayer;
import be.gim.specto.api.context.MapContext;
import be.gim.specto.api.context.RasterMapLayer;

import com.vividsolutions.jts.geom.Envelope;

@JsfComponent
public abstract class AbstractFeatureInfo extends AbstractFeature {

	public static final String COMPONENT_TYPE = "be.gim.specto.ui.FeatureInfo";

	protected List<? extends ModelObject> results;

	@Override
	public void resetLayer() {
		super.resetLayer();
		this.results = null;
	}

	@Override
	protected boolean isAvailable(RasterMapLayer layer) {
		return layer instanceof FeatureMapLayer
				&& ((FeatureMapLayer) layer).isQueryable();
	}

	@Override
	protected boolean hasData(RasterMapLayer layer) {
		return !layer.isHidden()
				&& getFeatureAsModel((FeatureMapLayer) layer).size() > 0
				&& getViewer().checkScale(layer, getScaleDenominator());
	}

	@Attribute
	public abstract Envelope getEnvelope();

	public abstract void setEnvelope(Envelope envelope);

	@Attribute
	public abstract Integer getWidth();

	public abstract void setWidth(Integer width);

	@Attribute
	public abstract Integer getHeight();

	public abstract void setHeight(Integer height);

	@Attribute
	public abstract Integer getI();

	public abstract void setI(Integer i);

	@Attribute
	public abstract Integer getJ();

	public abstract void setJ(Integer j);

	@Attribute
	public abstract Double getScaleDenominator();

	public abstract void setScaleDenominator(Double scaleDenominator);

	@Attribute
	public abstract Integer getTolerance();

	public abstract void setTolerance(Integer tolerance);

	public List<? extends ModelObject> getResults() {

		if (results == null) {
			RasterMapLayer layer = getLayer();

			if (layer instanceof FeatureMapLayer) {
				results = getFeatureAsModel((FeatureMapLayer) layer);
			} else {
				results = Collections.EMPTY_LIST;
			}
		}

		return results;
	}

	@SuppressWarnings("unchecked")
	protected List<? extends ModelObject> getFeatureAsModel(
			FeatureMapLayer layer) {

		MapViewer viewer = getViewer();
		MapContext context = viewer.getContext();

		List<? extends ModelObject> result = null;

		Integer i = getI();
		Integer j = getJ();
		Envelope envelope = getEnvelope();
		if (i != null && j != null) {
			int width = getWidth();
			int height = getHeight();

			result = (List<? extends ModelObject>) viewer
					.getFeatureInfoAsModel(layer, context.getSrsName(),
							envelope, context.getTime(), width, height, i, j,
							getTolerance(), null, null);
		} else if (envelope != null) {
			Filter filter = FilterUtils.intersects(new ReferencedEnvelope(
					envelope, context.getCrs()));
			if (!layer.getResultMode().isSelection()) {
				filter = FilterUtils.and(filter, layer.getFilter());
			}

			result = viewer.getFeatureAsModel(layer, context.getSrsName(),
					null, null, filter, null, null);
		}

		if (result != null) {
			return result;
		}

		return Collections.EMPTY_LIST;
	}

	public void update(String layerId, Integer width, Integer height,
			Integer i, Integer j, Envelope envelope, Double scaleDenominator,
			Integer tolerance) {

		reset();

		setLayerId(layerId);
		setI(i);
		setJ(j);
		setTolerance(tolerance);
		setScaleDenominator(scaleDenominator);
		setWidth(width);
		setHeight(height);
		setEnvelope(envelope);
	}

	public void updateBox(String layerId, Envelope envelope,
			Double scaleDenominator) {

		reset();

		setScaleDenominator(scaleDenominator);
		setLayerId(layerId);
		setEnvelope(envelope);
	}

	public void reset() {

		setI(null);
		setJ(null);
		setWidth(null);
		setHeight(null);
		setEnvelope(null);
		setScaleDenominator(null);

		resetLayer();
		getStateHelper().put(PropertyKeys.layerId, null);
	}
}
