package be.gim.tov.osyris.model.werk;

import java.util.Date;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Default;
import org.conscientia.api.model.annotation.Edit;
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
import org.conscientia.api.model.annotation.Required;
import org.conscientia.api.model.annotation.Search;
import org.conscientia.api.model.annotation.Target;
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
@Search(type = "stockMateriaal")
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
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('MagazijnCode')}")
	private String magazijn;

	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('StockCategorieCode')}")
	private String categorie;

	@Type(value = ModelPropertyType.ENUM)
	@Parents({ "categorie" })
	@ValuesExpression("#{osyrisModelFunctions.getStockSubCategories(parents[0])}")
	private String subCategorie;

	@Type(value = ModelPropertyType.ENUM)
	@Parents({ "categorie", "subCategorie" })
	@ValuesExpression("#{osyrisModelFunctions.getStockTypes(parents[0], parents[1])}")
	private String type;

	@Type(value = ModelPropertyType.ENUM)
	@Parents({ "categorie", "subCategorie", "type" })
	@ValuesExpression("#{osyrisModelFunctions.getStockMateriaalNamen(parents[0], parents[1], parents[2])}")
	private String naam;

	private String nummer;

	@Required
	@NotEditable
	@NotSearchable
	@Default("0")
	@Minimum(value = 0)
	private int inStock;

	@Required
	@NotSearchable
	@Default("5")
	@Minimum(value = 0)
	private int min;

	@Required
	@NotSearchable
	@Default("10")
	@Minimum(value = 0)
	private int max;

	@NotSearchable
	@Default("0")
	@Minimum(value = 0)
	private int teBestellen;

	@NotEditable
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.stockMateriaalStates}")
	private String besteld;

	@NotSearchable
	@NotEditable
	@NotViewable
	@ModelClassName("Traject")
	@Target("_blank")
	private ResourceIdentifier traject;

	@ModelClassName("Regio")
	@Edit(type = "menu")
	@Search(type = "menu:equals")
	@ValuesExpression("#{osyrisModelFunctions.getRegiosUitvoerder()}")
	@Target("_blank")
	private ResourceIdentifier regio;

	@Type(ModelPropertyType.DATE)
	private Date datumBesteld;

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
