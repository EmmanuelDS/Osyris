package be.gim.tov.osyris.model.user;

import java.util.Date;

import javax.enterprise.inject.Specializes;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.annotation.Column;
import org.conscientia.api.model.annotation.ContentType;
import org.conscientia.api.model.annotation.Edit;
import org.conscientia.api.model.annotation.FileSize;
import org.conscientia.api.model.annotation.Height;
import org.conscientia.api.model.annotation.Index;
import org.conscientia.api.model.annotation.Length;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.NotViewable;
import org.conscientia.api.model.annotation.Pattern;
import org.conscientia.api.model.annotation.Permission;
import org.conscientia.api.model.annotation.Permissions;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.api.model.annotation.View;
import org.conscientia.api.model.select.Level;
import org.conscientia.api.user.Gender;
import org.conscientia.core.user.DefaultUserProfile;

@Specializes
@Permissions({
		@Permission(profile = "group:Medewerker", action = "search", allow = true),
		@Permission(profile = "group:Medewerker", action = "view", allow = true),
		@Permission(profile = "group:Medewerker", action = "create", allow = true),
		@Permission(profile = "group:Medewerker", action = "edit", allow = true),
		@Permission(profile = "group:Medewerker", action = "delete", allow = true),

		@Permission(profile = "group:Routedokter", action = "search", allow = true),
		@Permission(profile = "group:Routedokter", action = "view", allow = true),
		@Permission(profile = "group:Routedokter", action = "create", allow = true),
		@Permission(profile = "group:Routedokter", action = "edit", allow = true),
		@Permission(profile = "group:Routedokter", action = "delete", allow = true) })
public class OsyrisUserProfile extends DefaultUserProfile {

	protected Gender gender;

	@NotViewable
	@NotSearchable
	@Type(ModelPropertyType.DATE)
	protected Date birthDate;

	@NotViewable
	@NotSearchable
	@Index
	@Length(min = 3, max = 30)
	@Pattern(PHONE_PATTERN)
	protected String phone;

	@NotViewable
	@NotSearchable
	@Length(min = 3, max = 30)
	@Pattern(PHONE_PATTERN)
	protected String cellPhone;

	@NotViewable
	@NotSearchable
	@Index
	@Length(min = 3, max = 30)
	@Pattern(PHONE_PATTERN)
	protected String fax;

	@Length(max = 100)
	protected String country;
	@Length(max = 100)
	protected String state;
	@Length(max = 100)
	protected String city;
	@NotViewable
	@NotSearchable
	@Length(min = 3, max = 10)
	protected String postalCode;
	@NotViewable
	@NotSearchable
	protected String address;

	@Length(max = 100)
	protected String organization;
	@Length(max = 100)
	protected String occupation;

	@Column("image")
	@View(type = "image", level = Level.FULL)
	@Edit(level = Level.LONG)
	@NotSearchable
	@ContentType("image/*")
	@FileSize(256 * 1024)
	@Height(128)
	protected byte[] avatar;

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getCellPhone() {
		return cellPhone;
	}

	public void setCellPhone(String cellPhone) {
		this.cellPhone = cellPhone;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getOccupation() {
		return occupation;
	}

	public void setOccupation(String occupation) {
		this.occupation = occupation;
	}

	public byte[] getAvatar() {
		return avatar;
	}

	public void setAvatar(byte[] avatar) {
		this.avatar = avatar;
	}
}