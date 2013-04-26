package be.gim.tov.osyris.model.controle;

import static org.conscientia.api.model.SubClassPersistence.UNION;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Label;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.SubClassPersistence;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.core.model.AbstractModelObject;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
@SubClassPersistence(UNION)
public abstract class Probleem extends AbstractModelObject implements
		StorableObject {

	// VARIABLES
	@NotEditable
	@Label("Type")
	@Description("Type")
	private String type;

	@Label("Fiche")
	@Description("Fiche")
	private String fiche;

	@Label("Foto")
	@Description("Foto")
	private String foto;

	@Label("Commentaar")
	@Description("Commentaar")
	@Type(value = ModelPropertyType.TEXT)
	private String commentaar;

	// GETTERS AND SETTERS
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFiche() {
		return fiche;
	}

	public void setFiche(String fiche) {
		this.fiche = fiche;
	}

	public String getFoto() {
		return foto;
	}

	public void setFoto(String foto) {
		this.foto = foto;
	}

	public String getCommentaar() {
		return commentaar;
	}

	public void setCommentaar(String commentaar) {
		this.commentaar = commentaar;
	}
}