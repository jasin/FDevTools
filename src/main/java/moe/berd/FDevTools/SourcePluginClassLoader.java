package moe.berd.FDevTools;

import java.io.*;
import java.net.*;
import java.util.*;

public class SourcePluginClassLoader extends URLClassLoader {

	private String pluginPath;
	private SourcePluginLoader loader;
	private Map<String,Class> classes = new HashMap<>();
	
	public SourcePluginClassLoader(SourcePluginLoader loader, ClassLoader parent, File file, String pluginPath) throws MalformedURLException {
		
        super(new URL[]{file.toURI().toURL()},parent);
		this.loader=loader;
		this.pluginPath=pluginPath.replace("\\","/")+"/";
	}

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return this.findClass(name, true);
    }

    protected Class<?> findClass(String name, boolean checkGlobal) throws ClassNotFoundException {
        if(name.startsWith("cn.nukkit.") || name.startsWith("net.minecraft.")) {
            throw new ClassNotFoundException(name);
        }

        Class<?> result = classes.get(name);

        if(result == null) {
            if(checkGlobal) {
                result = loader.getClassByName(name);
            }

            if(result == null) {
                result = super.findClass(name);
                if(result != null) {
                    loader.setClass(name, result);
                }
            }

            classes.put(name, result);
        }

        return result;
    }
	
	@Override
	public URL findResource(String name) {
		try {
			File res=new File(this.pluginPath+"src/main/resources/"+name);
			if(res.exists()) {
				return res.toURI().toURL();
			}
		}
		catch(Exception e)
		{
			
		}
		return null;
	}
}
