package moe.berd.FDevTools;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;

import javax.tools.*;

import cn.nukkit.utils.*;
import cn.nukkit.plugin.*;
import cn.nukkit.command.*;

public class Main extends PluginBase
{
	private static Main obj=null;
	
	private JavaCompiler compiler=null;
	
	public static Main getInstance()
	{
		return obj;
	}
	
	public void onEnable()
	{
		File data=new File(this.getDataFolder(),"packed");
		if(!data.isDirectory() && !data.mkdirs())
		{
			this.getLogger().error("Can't create data folder");
		}
		if(obj==null)
		{
			obj=this;
		}
		if(this.checkCompiler()!=null)
		{
			this.getServer().getPluginManager().registerInterface(SourcePluginLoader.class);
			List<String> loaders=new ArrayList<>();
			loaders.add(SourcePluginLoader.class.getName());
			this.getServer().getPluginManager().loadPlugins(new File(this.getServer().getPluginPath()),loaders,true);
			this.getServer().enablePlugins(PluginLoadOrder.STARTUP);
		}
	}
	
	public JavaCompiler getCompiler()
	{
		return this.compiler;
	}
	
	@SuppressWarnings("unchecked")
	public JavaCompiler checkCompiler()
	{
		this.compiler=ToolProvider.getSystemJavaCompiler();
		if(this.compiler==null)
		{
			this.getLogger().info(TextFormat.AQUA+"Compiler not found,loading third-party compiler...");
			File jar_file=new File(this.getDataFolder(),"tools.jar");
			if(!jar_file.isFile())
			{
				this.getLogger().error(TextFormat.YELLOW+"Third-party compiler not found,please download and put tools.jar into "+TextFormat.AQUA+this.getDataFolder().toString());
				return null;
			}

			try
			{
				URLClassLoader loader=new URLClassLoader(new URL[]
				{
					jar_file.toURI().toURL()
				});
				Class tools=loader.loadClass("com.sun.tools.javac.api.JavacTool");
				this.compiler=(JavaCompiler)tools.asSubclass(JavaCompiler.class).getDeclaredConstructor().newInstance();
			}
			catch (Exception e)
			{
				this.getLogger().error("Can't load third-party compiler,please use JDK to launch Nukkit or download tools.jar and put it into data folder!");
				e.printStackTrace();
				return null;
			}
		}
		return this.compiler;
	}
	
	public static List<File> listFolder(File input,String filter)
	{
		List<File> result=new ArrayList<>();
		if(input!=null)
		{
			if(!input.isDirectory())
			{
				if(filter.equals("") || input.toString().endsWith("."+filter))
				{
					result.add(input);
				}
				return result;
			}
			File[] var3=input.listFiles();
			int var4=var3.length;
			for(int var5=0; var5<var4;++var5)
			{
				File f=var3[var5];
				result.addAll(listFolder(f,filter));
			}
		}
		return result;
	}
	
	public static void removeFolder(File input,String filter)
	{
		if(input!=null)
		{
			if(input.isFile())
			{
				if(filter.equals("") || input.toString().endsWith("."+filter))
				{
					input.delete();
				}
			}
			else if(input.isDirectory())
			{
				for(File f:input.listFiles())
				{
					removeFolder(f,filter);
				}
				input.delete();
			}
		}
	}
}
