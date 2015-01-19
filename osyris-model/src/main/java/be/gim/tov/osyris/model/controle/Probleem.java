package be.gim.tov.osyris.model.controle;

import static org.conscientia.api.model.SubClassPersistence.UNION;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.ContentType;
import org.conscientia.api.model.annotation.Edit;
import org.conscientia.api.model.annotation.FileSize;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.Required;
import org.conscientia.api.model.annotation.SubClassPersistence;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.api.model.annotation.View;
import org.conscientia.api.model.annotation.Width;
import org.conscientia.core.model.AbstractModelObject;
import org.hibernate.bytecode.internal.javassist.FieldHandled;
import org.hibernate.bytecode.internal.javassist.FieldHandler;

import be.gim.tov.osyris.model.controle.status.ProbleemStatus;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
@SubClassPersistence(UNION)
@Edit(type = "probleem")
public abstract class Probleem extends AbstractModelObject implements
		StorableObject, FieldHandled {

	// VARIABLES
	private ProbleemStatus status;

	@Required
	@NotSearchable
	@Type(value = ModelPropertyType.TEXT)
	private String commentaar;

	@View(type = "image")
	@NotSearchable
	@ContentType("image/*")
	@FileSize(2 * 1024 * 1024)
	@Width(250)
	private byte[] foto;

	private transient FieldHandler FIELD_HANDLER;

	// GETTERS AND SETTERS
	public ProbleemStatus getStatus() {
		return status;
	}

	public void setStatus(ProbleemStatus status) {
		this.status = status;
	}

	public String getCommentaar() {
		return commentaar;
	}

	public void setCommentaar(String commentaar) {
		this.commentaar = commentaar;
	}

	public byte[] getFoto() {
		if (FIELD_HANDLER != null)
			return (byte[]) FIELD_HANDLER.readObject(this, "foto", foto);
		else
			return foto;
	}

	public void setFoto(byte[] foto) {

		if (FIELD_HANDLER != null)
			this.foto = (byte[]) FIELD_HANDLER.writeObject(this, "foto",
					this.foto, foto);
		else
			this.foto = foto;
	}

	@Override
	public void setFieldHandler(FieldHandler handler) {
		this.FIELD_HANDLER = handler;
	}

	@Override
	public FieldHandler getFieldHandler() {
		return FIELD_HANDLER;
	}
}