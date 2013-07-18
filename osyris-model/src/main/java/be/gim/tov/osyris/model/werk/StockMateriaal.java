package be.gim.tov.osyris.model.werk;

import java.util.Date;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Edit;
import org.conscientia.api.model.annotation.Label;
import org.conscientia.api.model.annotation.Minimum;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.NotViewable;
import org.conscientia.api.model.annotation.Parents;
import org.conscientia.api.model.annotation.Permission;
import org.conscientia.api.model.annotation.Permissions;
import org.conscientia.api.model.annotation.Search;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.api.model.annotation.ValuesExpression;
import org.conscientia.core.model.AbstractModelObject;

import be.gim.commons.resource.ResourceIdentifier;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
@Label("Stockmateriaal")
@Permissions({
		@Permission(profile = "group:Medewerker", action = "search", allow = true),
		@Permission(profile = "group:Medewerker", action = "view", allow = true),

		@Permission(profile = "group:Routedokter", action = "search", allow = true),
		@Permission(profile = "group:Routedokter", action = "view", allow = true),
		@Permission(profile = "group:Routedokter", action = "create", allow = true),
		@Permission(profile = "group:Routedokter", action = "edit", allow = true),

		@Permission(profile = "group:Uitvoerder", action = "search", allow = true),
		@Permission(profile = "group:Uitvoerder", action = "view", allow = true) })
public class StockMateriaal extends AbstractModelObject implements
		StorableObject {

	// VARIABLES
	@Label("Magazijn")
	@Description("Magazijn")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('MagazijnCode')}")
	private String magazijn;

	@Label("Categorie")
	@Description("Categorie")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('StockCategorieCode')}")
	private String categorie;

	@Label("Subcategorie")
	@Description("Subcategorie")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getStockSubCategories(parents[0])}")
	@Parents({ "categorie" })
	private String subCategorie;

	@Label("Type")
	@Description("Type")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getStockTypes(parents[0], parents[1])}")
	@Parents({ "categorie", "subCategorie" })
	private String type;

	@Label("Naam")
	@Description("Naam")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getStockMateriaalNamen(parents[0], parents[1], parents[2])}")
	@Parents({ "categorie", "subCategorie", "type" })
	private String naam;

	@Label("Nummer")
	@Description("Nummer")
	private String nummer;

	@NotEditable
	@Label("In stock")
	@Description("In stock")
	@NotSearchable
	private int inStock;

	@Label("Minimum")
	@Description("Minimum")
	@NotSearchable
	@Minimum(value = 0)
	private int min;

	@Label("Maximum")
	@Description("Maximum")
	@NotSearchable
	@Minimum(value = 0)
	private int max;

	@NotSearchable
	@Label("Te bestellen")
	@Description("Te bestellen")
	private int teBestellen;

	@NotEditable
	@Label("Besteld")
	@Description("Besteld")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.stockMateriaalStates}")
	private String besteld;

	@NotSearchable
	@NotEditable
	@NotViewable
	@Label("Traject")
	@Description("Traject")
	@ModelClassName("Traject")
	private ResourceIdentifier traject;

	@Label("Regio")
	@Description("Regio")
	@ModelClassName("Regio")
	@Edit(type = "menu")
	@Search(type = "menu:equals")
	@ValuesExpression("#{osyrisModelFunctions.getRegiosUitvoerder()}")
	private ResourceIdentifier regio;

	@Label("Datum besteld")
	@Description("Datum besteld")
	@Type(ModelPropertyType.DATE)
	private Date datumBesteld;

	@Label("Datum geleverd")
	@Description("Datum geleverd")
	@Type(ModelPropertyType.DATE)
	private Date datumGeleverd;

	// GETTERS AND SETTERS
	public String getMagazijn() {
		return magazijn;
	}

	public void setMagazijn(String magazijn) {
		this.magazijn = magazijn;
	}

	public String getCategorie() {
		return categorie;
	}

	public void setCategorie(String categorie) {
		this.categorie = categorie;
	}

	public String getSubCategorie() {
		return subCategorie;
	}

	public void setSubCategorie(String subCategorie) {
		this.subCategorie = subCategorie;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getNaam() {
		return naam;
	}

	public void setNaam(String naam) {
		this.naam = naam;
	}

	public String getNummer() {
		return nummer;
	}

	public void setNummer(String nummer) {
		this.nummer = nummer;
	}

	public int getInStock() {
		return inStock;
	}

	public void setInStock(int inStock) {
		this.inStock = inStock;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getTeBestellen() {
		return teBestellen;
	}

	public void setTeBestellen(int teBestellen) {
		this.teBestellen = teBestellen;
	}

	public String getBesteld() {
		return besteld;
	}

	public void setBesteld(String besteld) {
		this.besteld = besteld;
	}

	public ResourceIdentifier getTraject() {
		return traject;
	}

	public void setTraject(ResourceIdentifier traject) {
		this.traject = traject;
	}

	public ResourceIdentifier getRegio() {
		return regio;
	}

	public void setRegio(ResourceIdentifier regio) {
		this.regio = regio;
	}

	public Date getDatumBesteld() {
		return datumBesteld;
	}

	public void setDatumBesteld(Date datumBesteld) {
		this.datumBesteld = datumBesteld;
	}

	public Date getDatumGeleverd() {
		return datumGeleverd;
	}

	public void setDatumGeleverd(Date datumGeleverd) {
		this.datumGeleverd = datumGeleverd;
	}
}
