/*
 * NOTE: This copyright does *not* cover user programs that use HQ
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 * "derived work".
 * 
 * Copyright (C) [2004-2008], Hyperic, Inc.
 * This file is part of HQ.
 * 
 * HQ is free software; you can redistribute it and/or modify
 * it under the terms version 2 of the GNU General Public License as
 * published by the Free Software Foundation. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package org.hyperic.hq.plugin.python;

import org.python.core.*;
import org.python.util.PythonInterpreter;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import org.hyperic.hq.product.PluginException;
import org.hyperic.hq.product.PluginManager;
import org.hyperic.hq.product.ProductPlugin;
import org.hyperic.hq.product.ScriptLanguagePlugin;
import org.hyperic.util.PluginLoader;

public class PythonLanguagePlugin
    extends ProductPlugin
    implements ScriptLanguagePlugin {

    private PythonInterpreter _interp;
    private Properties _props;

    public void init(PluginManager manager)
        throws PluginException {

        super.init(manager);
        _props = manager.getProperties();
        addScriptLanguage(this);
    }

    public String getExtension() {
        return "py";
    }

    private PythonInterpreter getInterpreter() {
        if (_interp == null) {
            PythonInterpreter.initialize(System.getProperties(),
                                         _props,
                                         new String[0]);
            _interp = new PythonInterpreter();
        }
        return _interp;
    }

    private void addPath(File file) {
        PyString dir = new PyString(file.getParent());
        PySystemState sys = Py.getSystemState();
        if (!sys.path.__contains__(dir)) {
            sys.path.append(dir);
        }
    }

    public Class loadClass(ClassLoader loader,
                           Properties properties,
                           InputStream is)
        throws PluginException {

        throw new PluginException("NOTIMPL");
    }

    public Class loadClass(ClassLoader loader,
                           Properties properties,
                           File file)
        throws PluginException {


        PythonInterpreter interp = getInterpreter();

        addPath(file);

        String name = file.getName();
        int ix = name.indexOf(getExtension());
        if (ix != -1) {
            name = name.substring(0, ix-1);
        }

        interp.exec("import " + name);
        PyModule module = (PyModule)interp.get(name);
        PyObject cls = module.__findattr__(name.intern());
        if (cls == null) {
            throw new PluginException("Failed to find class: " + name);
        }
        else {
            return (Class)cls.__tojava__(Class.class);
        }
    }
}
