package be.gim.tov.osyris.model.werk;

import java.util.Date;
import java.util.List;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.core.model.AbstractModelObject;

import be.gim.commons.resource.ResourceIdentifier;
import be.gim.tov.osyris.model.controle.Probleem;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
public class WerkOpdracht extends AbstractModelObject implements StorableObject {

	// VARIABLES
	private String type;
	private String status;
	private String beoordeling;
	private String omschrijvingOpdracht;
	private String commentaarMedewerker;
	private String omschrijvingUitvoering;
	private String commentaarUitvoerder;
	private String foto;
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumGeannuleerd;
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumGerapporteerd;
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumGevalideerd;
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumTeControleren;
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumUitTeVoeren;
	@ModelClassName("User")
	private ResourceIdentifier medewerker;
	@ModelClassName("User")
	private ResourceIdentifier uitvoerder;
	private Probleem probleem;
	private GebruiktMateriaal gebruiktMateriaal;
	private List<WerkHandeling> handelingen;

	// GETTERS AND SETTERS
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getBeoordeling() {
		return beoordeling;
	}

	public void setBeoordeling(String beoordeling) {
		this.beoordeling = beoordeling;
	}

	public String getOmschrijvingOpdracht() {
		return omschrijvingOpdracht;
	}

	public void setOmschrijvingOpdracht(String omschrijvingOpdracht) {
		this.omschrijvingOpdracht = omschrijvingOpdracht;
	}

	public String getCommentaarMedewerker() {
		return commentaarMedewerker;
	}

	public void setCommentaarMedewerker(String commentaarMedewerker) {
		this.commentaarMedewerker = commentaarMedewerker;
	}

	public String getOmschrijvingUitvoering() {
		return omschrijvingUitvoering;
	}

	public void setOmschrijvingUitvoering(String omschrijvingUitvoering) {
		this.omschrijvingUitvoering = omschrijvingUitvoering;
	}

	public String getCommentaarUitvoerder() {
		return commentaarUitvoerder;
	}

	public void setCommentaarUitvoerder(String commentaarUitvoerder) {
		this.commentaarUitvoerder = commentaarUitvoerder;
	}

	public String getFoto() {
		return foto;
	}

	public void setFoto(String foto) {
		this.foto = foto;
	}

	public Date getDatumGeannuleerd() {
		return datumGeannuleerd;
	}

	public void setDatumGeannuleerd(Date datumGeannuleerd) {
		this.datumGeannuleerd = datumGeannuleerd;
	}

	public Date getDatumGerapporteerd() {
		return datumGerapporteerd;
	}

	public void setDatumGerapporteerd(Date datumGerapporteerd) {
		this.datumGerapporteerd = datumGerapporteerd;
	}

	public Date getDatumGevalideerd() {
		return datumGevalideerd;
	}

	public void setDatumGevalideerd(Date datumGevalideerd) {
		this.datumGevalideerd = datumGevalideerd;
	}

	public Date getDatumTeControleren() {
		return datumTeControleren;
	}

	public void setDatumTeControleren(Date datumTeControleren) {
		this.datumTeControleren = datumTeControleren;
	}

	public Date getDatumUitTeVoeren() {
		return datumUitTeVoeren;
	}

	public void setDatumUitTeVoeren(Date datumUitTeVoeren) {
		this.datumUitTeVoeren = datumUitTeVoeren;
	}

	public ResourceIdentifier getMedewerker() {
		return medewerker;
	}

	public void setMedewerker(ResourceIdentifier medewerker) {
		this.medewerker = medewerker;
	}

	public ResourceIdentifier getUitvoerder() {
		return uitvoerder;
	}

	public void setUitvoerder(ResourceIdentifier uitvoerder) {
		this.uitvoerder = uitvoerder;
	}

	public Probleem getProbleem() {
		return probleem;
	}

	public void setProbleem(Probleem probleem) {
		this.probleem = probleem;
	}

	public GebruiktMateriaal getGebruiktMateriaal() {
		return gebruiktMateriaal;
	}

	public void setGebruiktMateriaal(GebruiktMateriaal gebruiktMateriaal) {
		this.gebruiktMateriaal = gebruiktMateriaal;
	}

	public List<WerkHandeling> getHandelingen() {
		return handelingen;
	}

	public void setHandelingen(List<WerkHandeling> handelingen) {
		this.handelingen = handelingen;
	}
}