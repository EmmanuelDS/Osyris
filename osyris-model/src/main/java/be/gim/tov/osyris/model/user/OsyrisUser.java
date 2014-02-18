package be.gim.tov.osyris.model.user;

import javax.enterprise.inject.Specializes;

import org.conscientia.api.model.annotation.Edit;
import org.conscientia.api.model.annotation.Icon;
import org.conscientia.api.model.annotation.Permission;
import org.conscientia.api.model.annotation.Permissions;
import org.conscientia.api.model.annotation.Result;
import org.conscientia.api.model.annotation.Search;
import org.conscientia.api.model.annotation.Space;
import org.conscientia.api.model.annotation.View;
import org.conscientia.api.user.User;
import org.conscientia.core.user.DefaultUser;

@Specializes
@Space(User.USER_SPACE)
@Icon("user")
@View(type = "user")
@Edit(type = "user")
@Search(type = "user", boost = 0.6f)
@Result(type = "user")
@Permissions({
		@Permission(profile = "user:guest", action = "search", allow = false),
		@Permission(profile = "user:guest", action = "view", allow = false),
		@Permission(profile = "user:owner", action = "view", allow = true),
		@Permission(profile = "user:owner", action = "edit", allow = true),

		@Permission(profile = "group:Medewerker", action = "create", allow = true),
		@Permission(profile = "group:Medewerker", action = "edit", allow = true),
		@Permission(profile = "group:Medewerker", action = "clone", allow = true),
		@Permission(profile = "group:Medewerker", action = "delete", allow = true),

		@Permission(profile = "group:Routedokter", action = "create", allow = true),
		@Permission(profile = "group:Routedokter", action = "edit", allow = true),
		@Permission(profile = "group:Routedokter", action = "clone", allow = true),
		@Permission(profile = "group:Routedokter", action = "delete", allow = true),

		@Permission(action = "create", allow = false),
		@Permission(action = "edit", allow = false),
		@Permission(action = "clone", allow = false),
		@Permission(action = "delete", allow = false) })
public class OsyrisUser extends DefaultUser {
}