package be.gim.tov.osyris.model.controle;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.NotViewable;
import org.conscientia.api.model.annotation.Required;
import org.conscientia.api.model.annotation.SrsName;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.api.model.annotation.ValuesExpression;
import org.hibernate.bytecode.internal.javassist.FieldHandled;

import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
public abstract class AnderProbleem extends Probleem implements FieldHandled {

	// VARIABLES
	@Required
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('AnderProbleemCategorieCode')}")
	private String categorie;

	@NotViewable
	@NotEditable
	@NotSearchable
	@SrsName("EPSG:31370")
	private Geometry geom;

	@NotViewable
	@NotEditable
	@NotSearchable
	@SrsName("EPSG:31370")
	private Geometry geomOmleiding;

	// GETTERS AND SETTERS
	public Geometry getGeom() {
		return geom;
	}

	public void setGeom(Geometry geom) {
		this.geom = geom;
	}

	public String getCategorie() {
		return categorie;
	}

	public void setCategorie(String categorie) {
		this.categorie = categorie;
	}

	public Geometry getGeomOmleiding() {
		return geomOmleiding;
	}

	public void setGeomOmleiding(Geometry geomOmleiding) {
		this.geomOmleiding = geomOmleiding;
	}
}