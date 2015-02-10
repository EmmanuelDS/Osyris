package be.gim.tov.osyris.model.controle;

import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelStore;
import org.hibernate.bytecode.internal.javassist.FieldHandled;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
public class NetwerkAnderProbleem extends AnderProbleem implements FieldHandled {

}