package com.topcoder.shared.problem;

import com.topcoder.shared.netCommon.*;

import java.io.*;

public class ComponentCategory implements Serializable, Cloneable, CustomSerializable {
    String name;
    boolean checked;
    int id;
    public ComponentCategory(){
    }
    public ComponentCategory(String name, boolean checked, int id){
        this.name = name;
        this.checked = checked;
        this.id = id;
    }
	/** Custom serialization */
    public void customWriteObject(CSWriter writer) throws IOException{
        writer.writeString(name);
        writer.writeBoolean(checked);
        writer.writeInt(id);
    }

	/** Custom serialization */
    public void customReadObject(CSReader reader) throws IOException, ObjectStreamException{
        name = reader.readString();
        checked = reader.readBoolean();
        id = reader.readInt();
    }
    public String getName(){
        return name;
    }
    public boolean getChecked(){
        return checked;
    }
    public int getId(){
        return id;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setChecked(boolean checked){
        this.checked = checked;
    }
    public void setId(int id){
        this.id = id;
    }
}
