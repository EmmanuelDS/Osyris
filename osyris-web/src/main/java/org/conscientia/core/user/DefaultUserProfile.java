package org.conscientia.core.user;

import java.util.Date;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.annotation.ContentType;
import org.conscientia.api.model.annotation.External;
import org.conscientia.api.model.annotation.FileSize;
import org.conscientia.api.model.annotation.Icon;
import org.conscientia.api.model.annotation.Ignore;
import org.conscientia.api.model.annotation.Index;
import org.conscientia.api.model.annotation.Length;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.NotViewable;
import org.conscientia.api.model.annotation.Pattern;
import org.conscientia.api.model.annotation.Required;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.api.user.Gender;
import org.conscientia.api.user.UserProfile;
import org.conscientia.core.model.AbstractModelObject;

import be.gim.commons.localization.InternationalString;
import be.gim.commons.resource.ResourceIdentifier;

@Model("UserProfile")
@Icon("userProfile")
public class DefaultUserProfile extends AbstractModelObject implements
		UserProfile {

	@Required
	@NotViewable
	@NotEditable
	@NotSearchable
	@Index
	protected ResourceIdentifier _for;

	protected String firstName;
	protected String lastName;

	@NotViewable
	@NotSearchable
	@Type(ModelPropertyType.DATE)
	protected Date birthDate;

	protected Gender gender;

	@Override
	public ResourceIdentifier getFor() {
		return _for;
	}

	@Override
	public void setFor(ResourceIdentifier _for) {
		this._for = _for;
	}

	@NotViewable
	@NotSearchable
	@Required
	@Length(min = 3, max = 128)
	@Pattern("[\\w%\\.\\+\\-]+@[\\w%\\.\\+\\-]+\\.[a-zA-Z0-9]{2,4}")
	protected String email;

	@NotViewable
	@NotSearchable
	@Length(min = 3, max = 30)
	@Pattern("(\\+?[\\d]+)?")
	protected String phone;
	@NotViewable
	@NotSearchable
	@Length(min = 3, max = 30)
	@Pattern("(\\+?[\\d]+)?")
	protected String fax;

	protected String country;
	protected String state;
	protected String city;
	@NotViewable
	@NotSearchable
	protected String postalCode;
	@NotViewable
	@NotSearchable
	protected String address;

	protected String organization;
	protected String occupation;
	@External
	@NotSearchable
	protected ResourceIdentifier website;

	@Type(ModelPropertyType.INTERNATIONAL_TEXT)
	@NotSearchable
	protected InternationalString biography;

	@NotViewable
	@NotEditable
	// @View(type = "image", level = Level.FULL)
	// @Edit(level = Level.LONG)
	@NotSearchable
	@ContentType("image/*")
	@FileSize(524288)
	protected byte[] image;

	@Override
	public String getFirstName() {
		return firstName;
	}

	@Override
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Override
	public String getLastName() {
		return lastName;
	}

	@Override
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Override
	@Ignore
	public String getFullName() {
		return firstName + " " + lastName;
	}

	@Override
	public Date getBirthDate() {
		return birthDate;
	}

	@Override
	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	@Override
	public Gender getGender() {
		return gender;
	}

	@Override
	public void setGender(Gender gender) {
		this.gender = gender;
	}

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	@Override
	public String getCountry() {
		return country;
	}

	@Override
	public void setCountry(String country) {
		this.country = country;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Override
	public String getCity() {
		return city;
	}

	@Override
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

	@Override
	public String getOrganization() {
		return organization;
	}

	@Override
	public void setOrganization(String organization) {
		this.organization = organization;
	}

	@Override
	public String getOccupation() {
		return occupation;
	}

	@Override
	public void setOccupation(String occupation) {
		this.occupation = occupation;
	}

	@Override
	public ResourceIdentifier getWebsite() {
		return website;
	}

	@Override
	public void setWebsite(ResourceIdentifier website) {
		this.website = website;
	}

	@Override
	public InternationalString getBiography() {
		return biography;
	}

	@Override
	public void setBiography(InternationalString biography) {
		this.biography = biography;
	}

	@Override
	public byte[] getImage() {
		return image;
	}

	@Override
	public void setImage(byte[] image) {
		this.image = image;
	}
}