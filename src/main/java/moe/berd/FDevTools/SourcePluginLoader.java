package moe.berd.FDevTools;

import java.io.*;
import java.nio.file.*;

import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

import javax.tools.*;
import javax.tools.JavaCompiler.*;

import cn.nukkit.*;
import cn.nukkit.utils.*;
import cn.nukkit.plugin.*;
import cn.nukkit.event.plugin.*;

public class SourcePluginLoader implements PluginLoader
{
	private Main plugin=null;
	private Server server=null;
	
	private JavaCompiler compiler=null;
	private StandardJavaFileManager filemanager=null;
	
	private Map<String,Class> classes=new HashMap<>();
	private Map<PluginBase,File> pluginPath=new HashMap<>();
	private Map<String,SourcePluginClassLoader> classLoaders=new HashMap<>();
	
	public SourcePluginLoader(Server server) {
		this.server=server;
		this.plugin=Main.getInstance();
		this.compiler=this.plugin.getCompiler();
		this.filemanager=this.compiler.getStandardFileManager(null,null,null);
	}
	
    @Override
	public Plugin loadPlugin(File dir) throws Exception {
		PluginDescription description = this.getPluginDescription(dir);
		if(description != null) {
			this.plugin.getLogger().info(TextFormat.AQUA+"Loading source plugin \""+description.getName()+"\"");
			File class_file = new File(dir.getAbsolutePath()+"/src_compile");
			this.compilePlugin(dir,class_file);
			SourcePluginClassLoader classLoader = new SourcePluginClassLoader(this, this.getClass().getClassLoader(), class_file, dir.getAbsolutePath());
			this.classLoaders.put(description.getName(),classLoader);
			File dataFolder = new File(dir.getParentFile(), description.getName());
			if(dataFolder.exists() && !dataFolder.isDirectory()) {
				throw new IllegalStateException("Compile target folder \""+dataFolder.toString()+"\" for "+description.getName()+" exists and is not a directory");
			}

            PluginBase plugin;
			try {
			    Class sourcePluginClass = classLoader.loadClass(description.getMain());

                if(!PluginBase.class.isAssignableFrom(sourcePluginClass)) {
                    throw new PluginException("Main class '" + description.getMain() + "' does not extend PluginBase");
                }

                try {
                    Class<PluginBase> pluginClass = (Class<PluginBase>) sourcePluginClass.asSubclass(PluginBase.class);
                    plugin = pluginClass.newInstance();
                    
                    this.initPlugin(plugin, description, dataFolder, dir);
					this.pluginPath.put(plugin, dir);
					return plugin;
				} catch(ClassCastException e) {
					throw new PluginException("Main class \""+description.getMain()+"\" does not extend PluginBase");
				} catch(InstantiationException | IllegalAccessException e) {
					Server.getInstance().getLogger().logException(e);
                }
		    } catch(ClassNotFoundException e) {
                throw new PluginException("Couldn't load plugin " + description.getName() + ": main class not found");
            }
        }
		
        return null;
	}
	
    @Override
	public PluginDescription getPluginDescription(File dir) {
		try {
			File yml=new File(dir.getAbsolutePath() + "/src/main/resources", "plugin.yml");
			return dir.isDirectory() && yml.isFile() ? new PluginDescription(Utils.readFile(yml)) : null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
    @Override
	public Pattern[] getPluginFilters()	{
		return new Pattern[]{Pattern.compile(".+")};
	}
	
	@SuppressWarnings("unchecked")
	public boolean compilePlugin(File dir,File output) throws Exception
	{
		if(output.isFile())
		{
			throw new Exception("Compile output path exists and isn't a directory");
		}
		else
		{
			Main.removeFolder(output,"class");
			if(!output.mkdirs())
			{
				throw new Exception("Couldn't create compile output directory");
			}
			else
			{
				List files=Main.listFolder(new File(dir.getAbsolutePath()+"/src"),"java");
				CompilationTask task=this.compiler.getTask(null,this.filemanager,null,Arrays.asList(new String[]
				{
					"-d",
					output.getAbsolutePath(),
					"-encoding",
					"utf-8"
				}),null,this.filemanager.getJavaFileObjects((File[])files.toArray(new File[files.size()])));
				return task.call().booleanValue();
			}
		}
	}
	
    public File getPluginPath(Plugin p) {
        return this.pluginPath.getOrDefault(p, null);
    }
   
	
    public PluginDescription getPluginDescription(String filename) {
		return this.getPluginDescription(new File(filename));
	}
	
	public Plugin loadPlugin(String filename) throws Exception {
		return this.loadPlugin(new File(filename));
	}
	
	private void initPlugin(PluginBase plugin, PluginDescription description, File dataFolder, File file) {
		plugin.init(this, this.server, description, dataFolder, file);
		plugin.onLoad();
	}
	
    @Override
	public void enablePlugin(Plugin plugin) {
		if(plugin instanceof PluginBase && !plugin.isEnabled()) {
			this.server.getLogger().info(this.server.getLanguage().translateString("nukkit.plugin.enable", plugin.getDescription().getFullName()));
			((PluginBase) plugin).setEnabled(true);
			this.server.getPluginManager().callEvent(new PluginEnableEvent(plugin));
		}
	}
	
    @Override
	public void disablePlugin(Plugin plugin) {
		if(plugin instanceof PluginBase && plugin.isEnabled()) {
			this.server.getLogger().info(this.server.getLanguage().translateString("nukkit.plugin.disable",plugin.getDescription().getFullName()));
            this.server.getServiceManager().cancel(plugin);
			this.server.getPluginManager().callEvent(new PluginDisableEvent(plugin));
			((PluginBase) plugin).setEnabled(false);
		}
	}
	
	public Class<?> getClassByName(String name)	{
		Class<?> cachedClass=classes.get(name);

		if(cachedClass != null) {
			return cachedClass;
		} else {
			for(SourcePluginClassLoader loader : this.classLoaders.values()) {
                
                try	{
                    cachedClass=loader.findClass(name,false);
                } catch(ClassNotFoundException e) {
                    //this.plugin.getLogger().error(e.toString());
                }

                if(cachedClass != null) {
                    return cachedClass;
                }
            }
		}
        return null;
	}
	
	public void setClass(String name, Class<?> clazz) {
		if(!this.classes.containsKey(name)) {
			this.classes.put(name, clazz);
		}
	}
	
	private void removeClass(String name) {
		this.classes.remove(name);
	}
}
