package me.simplicitee.project.arenas.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import net.minecraft.server.v1_15_R1.NBTBase;
import net.minecraft.server.v1_15_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagList;
import net.minecraft.server.v1_15_R1.NBTTagString;

public class NBTStorageFile {

	private final File file;
	private NBTTagCompound tagCompound;

	public NBTStorageFile(File file) {
		this.file = file;
	}

	public NBTStorageFile(String folder, String name) {
		this(new File(folder, name + ".dat"));
	}
	
	public NBTStorageFile(File folder, String name) {
		this(new File(folder, name + ".dat"));
	}

	public NBTStorageFile(String path) {
		this(new File(path));
	}

	public NBTStorageFile read() {
		try {
			if (file.exists()) {
				FileInputStream fileinputstream = new FileInputStream(file);
				tagCompound = NBTCompressedStreamTools.a(fileinputstream);
				fileinputstream.close();
			} else {
				clear();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return this;
	}

	public NBTStorageFile write() {
		try {
			if (!file.exists()) {
				file.createNewFile();
			}

			FileOutputStream fileoutputstream = new FileOutputStream(file);
			NBTCompressedStreamTools.a(tagCompound, fileoutputstream);
			fileoutputstream.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return this;
	}

	public void clear() {
		tagCompound = new NBTTagCompound();
	}

	public Set<String> getKeys() {
		return tagCompound.getKeys();
	}

	public boolean hasKey(String key) {
		return tagCompound.hasKey(key);
	}

	public boolean isEmpty() {
		return tagCompound.isEmpty();
	}

	public void remove(String key) {
		tagCompound.remove(key);
	}

	public boolean getBoolean(String key) {
		return tagCompound.getBoolean(key);
	}

	public double getDouble(String key) {
		return tagCompound.getDouble(key);
	}

	public float getFloat(String key) {
		return tagCompound.getFloat(key);
	}

	public int getInt(String key) {
		return tagCompound.getInt(key);
	}

	public int[] getIntArray(String key) {
		return tagCompound.getIntArray(key);
	}

	public long getLong(String key) {
		return tagCompound.getLong(key);
	}

	public short getShort(String key) {
		return tagCompound.getShort(key);
	}

	public String getString(String key) {
		return tagCompound.getString(key);
	}

	public void setBoolean(String key, boolean value) {
		tagCompound.setBoolean(key, value);
	}

	public void setDouble(String key, double value) {
		tagCompound.setDouble(key, value);
	}

	public void setFloat(String key, float value) {
		tagCompound.setFloat(key, value);
	}

	public void setInt(String key, int value) {
		tagCompound.setInt(key, value);
	}

	public void setIntArray(String key, int[] value) {
		tagCompound.setIntArray(key, value);
	}

	public void setLong(String key, long value) {
		tagCompound.setLong(key, value);
	}

	public void setShort(String key, short value) {
		tagCompound.setShort(key, value);
	}

	public void setString(String key, String value) {
		tagCompound.setString(key, value);
	}
	
	public void setStringList(String key, Collection<String> value) {
        // create a new List. This list can contain any NBTBase
        NBTTagList list = new NBTTagList();
       
        for(String s : value) {
            // if you want to store something else than a String, you create an other NBTbase here.
            // currently the NBTBases are: primitives, String, List and the compound itself.
            NBTTagString string = NBTTagString.a(s);
            list.add(string);
        }
        tagCompound.set(key, list);
    }
   
    public List<String> getStringList(String key) {
        List<String> result = Lists.newArrayList();
       
        NBTBase base = tagCompound.get(key);
        if(base != null && base.getClass() == NBTTagList.class) {
           
            NBTTagList list = (NBTTagList) base;
            for(int i = 0; i < list.size(); i++) {
                result.add(list.getString(i));
            }
        }
        return result;
    }
}
