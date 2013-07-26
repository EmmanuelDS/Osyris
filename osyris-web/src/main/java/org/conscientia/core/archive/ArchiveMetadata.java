package org.conscientia.core.archive;

import java.util.List;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.annotation.Icon;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.Required;
import org.conscientia.api.model.annotation.Transient;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.api.model.annotation.View;
import org.conscientia.core.model.AbstractModelObject;

import be.gim.commons.resource.ResourceName;

//@Model
@Transient
@Icon("archive")
public class ArchiveMetadata extends AbstractModelObject {

	public static final String ARCHIVE_FILENAME = "_archive.xml";

	@Required
	protected String name = "archive";
	protected String version = "1.0.0";
	@Type(ModelPropertyType.TEXT)
	protected String description;
	protected String license = "LGPL";
	protected String maintainer;
	protected boolean backup;
	@Type(name = "DocumentMetadataEntry")
	@Transient
	@View(type = "table")
	@NotEditable
	protected List<DocumentMetadataEntry> entries;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getMaintainer() {
		return maintainer;
	}

	public void setMaintainer(String maintainer) {
		this.maintainer = maintainer;
	}

	public boolean isBackup() {
		return backup;
	}

	public void setBackup(boolean backup) {
		this.backup = backup;
	}

	public List<DocumentMetadataEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<DocumentMetadataEntry> entries) {
		this.entries = entries;
	}

	public DocumentMetadataEntry getEntry(ResourceName name) {
		for (DocumentMetadataEntry documentEntry : entries) {
			if (name.equals(documentEntry.getName())) {
				return documentEntry;
			}
		}
		return null;
	}
}
