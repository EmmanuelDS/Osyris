package org.conscientia.core.archive;

import java.util.List;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.annotation.Icon;
import org.conscientia.api.model.annotation.Required;
import org.conscientia.api.model.annotation.Transient;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.api.model.annotation.View;
import org.conscientia.api.model.select.Level;
import org.conscientia.core.model.AbstractModelObject;

import be.gim.commons.resource.ResourceName;

//@Model
@Transient
@Icon("archive")
public class DocumentMetadataEntry extends AbstractModelObject {

	@Required
	protected ResourceName name;
	@View(level = Level.LONG)
	protected String filename;
	@Type(ModelPropertyType.RESOURCE_IDENTIFIER)
	protected List<ResourceName> dependencies;

	public DocumentMetadataEntry() {
	}

	public DocumentMetadataEntry(ResourceName name, String filename,
			List<ResourceName> dependencies) {
		this.name = name;
		this.filename = filename;
		this.dependencies = dependencies;
	}

	public ResourceName getName() {
		return name;
	}

	public void setName(ResourceName name) {
		this.name = name;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public List<ResourceName> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<ResourceName> dependencies) {
		this.dependencies = dependencies;
	}
}
