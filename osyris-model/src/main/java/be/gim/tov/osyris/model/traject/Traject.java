package be.gim.tov.osyris.model.traject;

import static org.conscientia.api.model.SubClassPersistence.UNION;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import javax.persistence.Transient;

import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.cache.CacheProducer;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Edit;
import org.conscientia.api.model.annotation.Index;
import org.conscientia.api.model.annotation.LabelProperty;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.NotViewable;
import org.conscientia.api.model.annotation.Search;
import org.conscientia.api.model.annotation.SrsName;
import org.conscientia.api.model.annotation.SubClassPersistence;
import org.conscientia.api.model.annotation.Target;
import org.conscientia.api.model.annotation.ValuesExpression;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.core.model.AbstractModelObject;

import be.gim.commons.bean.Beans;
import be.gim.commons.resource.ResourceIdentifier;
import be.gim.commons.resource.ResourceName;
import be.gim.tov.osyris.model.bean.OsyrisModelFunctions;
import be.gim.tov.osyris.model.user.MedewerkerProfiel;

import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
@SubClassPersistence(UNION)
@Search(type = "traject")
public abstract class Traject extends AbstractModelObject implements
		StorableObject {

	private static final Log LOG = LogFactory.getLog(Traject.class);

	// VARIABLES
	@Edit(type = "suggestions")
	@LabelProperty
	@Search(type = "suggestions:like-wildcard-nocase")
	private String naam;

	@ModelClassName("Regio")
	@Edit(type = "menu")
	@Search(type = "menu:equals")
	@ValuesExpression("#{osyrisModelFunctions.regiosOostVlaanderen}")
	@Target("_blank")
	private ResourceIdentifier regio;

	@NotEditable
	@NotSearchable
	private float lengte;

	@NotViewable
	@NotSearchable
	@NotEditable
	@SrsName("EPSG:31370")
	@Index
	private Geometry geom;

	@NotSearchable
	@ModelClassName("User")
	@Edit(type = "menu")
	@ValuesExpression("#{osyrisModelFunctions.getPeterMeterNaamCodes(true)}")
	@Search(type = "menu:equals")
	@Target("_blank")
	private ResourceIdentifier peterMeter1;

	@NotSearchable
	@ModelClassName("User")
	@Edit(type = "menu")
	@ValuesExpression("#{osyrisModelFunctions.getPeterMeterNaamCodes(true)}")
	@Target("_blank")
	private ResourceIdentifier peterMeter2;

	@NotSearchable
	@ModelClassName("User")
	@Edit(type = "menu")
	@ValuesExpression("#{osyrisModelFunctions.getPeterMeterNaamCodes(true)}")
	@Target("_blank")
	private ResourceIdentifier peterMeter3;

	// GETTERS AND SETTERS
	public String getNaam() {
		return naam;
	}

	public void setNaam(String naam) {
		this.naam = naam;
	}

	public ResourceIdentifier getRegio() {
		return regio;
	}

	public void setRegio(ResourceIdentifier regio) {
		this.regio = regio;
	}

	public float getLengte() {
		return lengte;
	}

	public void setLengte(float lengte) {
		this.lengte = lengte;
	}

	public Geometry getGeom() {
		return geom;
	}

	public void setGeom(Geometry geom) {
		this.geom = geom;
	}

	public ResourceIdentifier getPeterMeter1() {
		return peterMeter1;
	}

	public void setPeterMeter1(ResourceIdentifier peterMeter1) {
		this.peterMeter1 = peterMeter1;
	}

	public ResourceIdentifier getPeterMeter2() {
		return peterMeter2;
	}

	public void setPeterMeter2(ResourceIdentifier peterMeter2) {
		this.peterMeter2 = peterMeter2;
	}

	public ResourceIdentifier getPeterMeter3() {
		return peterMeter3;
	}

	public void setPeterMeter3(ResourceIdentifier peterMeter3) {
		this.peterMeter3 = peterMeter3;
	}

	@Transient
	@NotSearchable
	@NotEditable
	public ResourceIdentifier getMedewerker() {

		Map<Serializable, Object> cache = Beans.getReference(
				CacheProducer.class).getCache("trajectMedewerkerCache",
				new Transformer() {

					@Override
					public Object transform(Object key) {

						try {
							for (ResourceName user : Beans.getReference(
									OsyrisModelFunctions.class)
									.getUsersInGroup("Medewerker")) {

								MedewerkerProfiel profiel = (MedewerkerProfiel) Beans
										.getReference(ModelRepository.class)
										.loadAspect(
												Beans.getReference(
														ModelRepository.class)
														.getModelClass(
																"MedewerkerProfiel"),
												user);

								if (profiel != null
										&& profiel.getTrajectType().contains(
												key)) {
									return user;
								}
							}
						} catch (IOException e) {
							LOG.error("Can not search objects.", e);
						}

						return null;
					}
				});

		return (ResourceIdentifier) cache.get(getModelClass().getName());
	}
}