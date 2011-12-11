package org.iocaste.datadict;

import org.iocaste.shell.common.AbstractPage;
import org.iocaste.shell.common.Button;
import org.iocaste.shell.common.Const;
import org.iocaste.shell.common.Container;
import org.iocaste.shell.common.ControlData;
import org.iocaste.shell.common.DataForm;
import org.iocaste.shell.common.DataItem;
import org.iocaste.shell.common.Form;
import org.iocaste.shell.common.Table;
import org.iocaste.shell.common.TableItem;
import org.iocaste.shell.common.TextField;
import org.iocaste.shell.common.ViewData;

public class Main extends AbstractPage {

    public final void create(ControlData cdata, ViewData vdata) {
        String modelname = ((DataItem)vdata.getElement("modelname")).getValue();
        
        cdata.setReloadableView(true);
        cdata.addParameter("mode", "create");
        cdata.addParameter("modelname", modelname);
        cdata.redirect(null, "structure");
    }
    
    public final void main(ViewData view) {
        Container main = new Form(null, "datadict.main");
        DataForm modelform = new DataForm(main, "modelform");
        DataItem modelname = new DataItem(modelform, Const.TEXT_FIELD,
                "modelname");
        
        modelname.setObligatory(true);
        
        modelform.addAction("create");
        modelform.addAction("show");
        modelform.addAction("update");
        modelform.addAction("delete");
        
        view.setNavbarActionEnabled("back", true);
        view.setTitle("datadict.utilities");
        view.addContainer(main);
    }
    
    public final void show(ControlData cdata, ViewData vdata) {
        
    }
    
    public final void structure(ViewData vdata) {
        Container main = new Form(null, "datadict.structure");
        DataForm structure = new DataForm(main, "structure.form");
        String title = null, mode = (String)vdata.getParameter("mode");
        String name = ((String)vdata.getParameter("modelname"));
        DataItem modelname = new DataItem(structure, Const.TEXT_FIELD,
                "modelname");
        DataItem modeltext = new DataItem(structure, Const.TEXT_FIELD,
                "modeltext");
        DataItem modelclass = new DataItem(structure, Const.TEXT_FIELD,
                "modelclass");
        DataItem modeltable = new DataItem(structure, Const.TEXT_FIELD,
                "modeltable");
        Table itens = new Table(main, 3, "itens");
        TableItem item = new TableItem(itens);
        
        modelname.setValue(name);
        modelname.setEnabled(false);
        modeltext.setObligatory(true);
        modelclass.setObligatory(true);
        
        if (mode.equals("update")) {
            title = "datadict.update";
            new Button(main, "save");
            new Button(main, "add");
            new Button(main, "deleteitem");
        }
        
        if (mode.equals("show"))
            title = "datadict.view";
        
        if (mode.equals("create")) {
            title = "datadict.create";
            new Button(main, "save");
            new Button(main, "add");
            new Button(main, "deleteitem");
            
            item.add(new TextField(itens, "item.name"));
            item.add(new TextField(itens, "item.type"));
            item.add(new TextField(itens, "item.length"));
        }
        
        vdata.setNavbarActionEnabled("back", true);
        vdata.setTitle(title);
        vdata.addContainer(main);
    }
    
    public final void update(ControlData cdata, ViewData vdata) {

    }
}
