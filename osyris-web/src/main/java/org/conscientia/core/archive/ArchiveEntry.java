package org.conscientia.core.archive;

import java.util.List;

import org.conscientia.api.model.annotation.Icon;
import org.conscientia.api.model.annotation.Required;
import org.conscientia.api.model.annotation.Transient;
import org.conscientia.api.model.annotation.View;
import org.conscientia.api.model.select.Level;
import org.conscientia.core.model.AbstractModelObject;

//@Model
@Transient
@Icon("archive")
public class ArchiveEntry extends AbstractModelObject {

	@Required
	protected String entryClassName;
	@Required
	protected String entryId;
	@Required
	protected String entryName;
	@View(level = Level.LONG)
	protected String filename;
	protected List<String> dependencies;

	public ArchiveEntry() {
	}

	public ArchiveEntry(String entryClassName, String entryId,
			String entryName, String filename, List<String> dependencies) {
		this.entryClassName = entryClassName;
		this.entryId = entryId;
		this.entryName = entryName;
		this.filename = filename;
		this.dependencies = dependencies;
	}

	public String getEntryClassName() {
		return entryClassName;
	}

	public void setEntryClassName(String entryClassName) {
		this.entryClassName = entryClassName;
	}

	public String getEntryId() {
		return entryId;
	}

	public void setEntryId(String entryId) {
		this.entryId = entryId;
	}

	public String getEntryName() {
		return entryName;
	}

	public void setEntryName(String entryName) {
		this.entryName = entryName;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public List<String> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<String> dependencies) {
		this.dependencies = dependencies;
	}
}
