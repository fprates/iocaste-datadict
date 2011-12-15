package org.iocaste.datadict;

import org.iocaste.documents.common.DataElement;
import org.iocaste.documents.common.DataType;
import org.iocaste.documents.common.DocumentModel;
import org.iocaste.documents.common.DocumentModelItem;
import org.iocaste.documents.common.DocumentModelKey;
import org.iocaste.documents.common.Documents;
import org.iocaste.shell.common.AbstractPage;
import org.iocaste.shell.common.Button;
import org.iocaste.shell.common.CheckBox;
import org.iocaste.shell.common.Const;
import org.iocaste.shell.common.Container;
import org.iocaste.shell.common.ControlData;
import org.iocaste.shell.common.DataForm;
import org.iocaste.shell.common.DataItem;
import org.iocaste.shell.common.Element;
import org.iocaste.shell.common.Form;
import org.iocaste.shell.common.ListBox;
import org.iocaste.shell.common.Table;
import org.iocaste.shell.common.TableItem;
import org.iocaste.shell.common.ViewData;

public class Main extends AbstractPage {
    private static final String[] ITEM_NAMES = {
        "item.name",
        "item.tablefield",
        "item.classfield",
        "item.key", 
        "item.type",
        "item.length",
        "item.text"
    };
    
    public final void add(ControlData cdata, ViewData vdata) {
        Table itens = (Table)vdata.getElement("itens");
        
        insertitem(itens);
    }
    
    public final void create(ControlData cdata, ViewData vdata)
            throws Exception {
        Documents documents = new Documents(this);
        String modelname = ((DataItem)vdata.getElement("modelname")).getValue();
        
        if (documents.hasModel(modelname)) {
            cdata.message(Const.ERROR, "model.already.exist");
            return;
        }
            
        cdata.setReloadableView(true);
        cdata.addParameter("mode", "create");
        cdata.addParameter("modelname", modelname);
        cdata.redirect(null, "structure");
    }
    
    public final void delete(ControlData cdata, ViewData vdata)
            throws Exception {
        Documents documents = new Documents(this);
        String modelname = ((DataItem)vdata.getElement("modelname")).getValue();
        
        if (!documents.hasModel(modelname)) {
            cdata.message(Const.ERROR, "model.not.found");
            return;
        }
        
        documents.removeModel(modelname);
        documents.commit();
        
        cdata.message(Const.STATUS, "model.removed.sucessfully");
    }
    
    public final void deleteitem(ControlData cdata, ViewData vdata) {
        Table itens = (Table)vdata.getElement("itens");
        TableItem[] marked = itens.getSelected();
        
        for (TableItem item : marked)
            itens.remove(item);
    }
    
    private final void insertitem(Table itens) {
        ListBox list;
        TableItem item = new TableItem(itens);
        
        for (String name: ITEM_NAMES) {
            if (name.equals("item.key")) {
                item.add(Const.CHECKBOX, name, null);
                
                continue;
            }
        
            if (name.equals("item.type")) {
                item.add(Const.LIST_BOX, name, null);
                
                list = (ListBox)itens.getElement(
                        item.getComplexName("item.type"));
                list.add("char", Integer.toString(DataType.CHAR));
                list.add("numc", Integer.toString(DataType.NUMC));
                
                continue;
            }
            
            item.add(Const.TEXT_FIELD, name, null);
        }
    }
    
    public final void main(ViewData view) {
        Container main = new Form(null, "datadict.main");
        DataForm modelform = new DataForm(main, "modelform");
        DataItem modelname = new DataItem(modelform, Const.TEXT_FIELD,
                "modelname");
        
        modelname.setObligatory(true);
        
        modelform.addAction("create");
//        modelform.addAction("show");
//        modelform.addAction("update");
        modelform.addAction("delete");
        
        view.setFocus("modelname");
        view.setNavbarActionEnabled("back", true);
        view.setTitle("datadict.utilities");
        view.addContainer(main);
    }
    
    public final void save(ControlData cdata, ViewData vdata) throws Exception {
        TableItem item;
        DocumentModelItem modelitem;
        DocumentModelKey modelkey;
        DataElement dataelement;
        String itemname;
        CheckBox key;
        Documents documents = new Documents(this);
        DataForm structure = (DataForm)vdata.getElement("structure.form");
        Table itens = (Table)vdata.getElement("itens");
        DocumentModel model = new DocumentModel();
        int i = 0;
        
        model.setName(structure.getValue("modelname"));
        model.setClassName(structure.getValue("modelclass"));
        model.setTableName(structure.getValue("modeltable"));
        
        for (Element element : itens.getElements()) {
            if (element.getType() != Const.TABLE_ITEM)
                continue;
            
            item = (TableItem)element;
            itemname = itens.getValue(item, "item.name");
            
            dataelement = new DataElement();
            dataelement.setName(new StringBuilder(model.getName()).append(".").
                    append(itemname).toString());
            dataelement.setLength(Integer.parseInt(
                    itens.getValue(item, "item.length")));
            dataelement.setType(Integer.parseInt(
                    itens.getValue(item, "item.type")));
             
            modelitem = new DocumentModelItem();
            modelitem.setIndex(i++);
            modelitem.setName(itemname);
            modelitem.setTableFieldName(itens.getValue(item,
                    "item.tablefield"));
            modelitem.setAttributeName(itens.getValue(item, "item.classfield"));
            modelitem.setDataElement(dataelement);
            modelitem.setDocumentModel(model);
            
            model.add(modelitem);
            
            key = (CheckBox)itens.getElement(item.getComplexName("item.key"));
            if (!key.isSelected())
                continue;
        
            modelkey = new DocumentModelKey();
            modelkey.setModel(model);
            modelkey.setModelItem(itemname);
            
            model.addKey(modelkey);
        }
        
        documents.createModel(model);
        documents.commit();
        
        cdata.message(Const.STATUS, "table.saved.successfully");
        back(cdata, vdata);
    }
    
    /**
     * 
     * @param cdata
     * @param vdata
     */
    public final void show(ControlData cdata, ViewData vdata) {
        String modelname = ((DataItem)vdata.getElement("modelname")).getValue();
        
        cdata.setReloadableView(true);
        cdata.addParameter("mode", "show");
        cdata.addParameter("modelname", modelname);
        cdata.redirect(null, "structure");
    }
    
    /**
     * 
     * @return
     */
    private final DocumentModel getItensModel(String modelname) {
        DataElement dataelement;
        DocumentModelItem item;
        DocumentModel model = new DocumentModel();
        int i = 0;
        
        model.setName(modelname);
        
        for (String name : ITEM_NAMES) {
            dataelement = new DataElement();
            if (name.equals("item.length")) {
                dataelement.setType(DataType.NUMC);
                dataelement.setLength(3);
            } else {
                dataelement.setType(DataType.CHAR);
                dataelement.setLength(20);
            }
            
            item = new DocumentModelItem();
            item.setName(name);
            item.setDocumentModel(model);
            item.setDataElement(dataelement);
            item.setIndex(i++);
            
            model.add(item);
        }
        
        return model;
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
        Table itens = new Table(main, 0, "itens");
        DocumentModel model = getItensModel("itens");
        
        modelname.setValue(name);
        modelname.setEnabled(false);
        modeltext.setObligatory(true);
        modelclass.setObligatory(true);
        modeltable.setObligatory(true);
        
        itens.setMark(true);
        itens.importModel(model);
        
        if (mode.equals("update")) {
            title = "datadict.update";
            new Button(main, "save");
            new Button(main, "add");
            new Button(main, "deleteitem");
            
            insertitem(itens);
        }
        
        if (mode.equals("show"))
            title = "datadict.view";
        
        if (mode.equals("create")) {
            title = "datadict.create";
            new Button(main, "save");
            new Button(main, "add");
            new Button(main, "deleteitem");
            
            insertitem(itens);
        }
        
        vdata.setFocus("modeltext");
        vdata.setNavbarActionEnabled("back", true);
        vdata.setTitle(title);
        vdata.addContainer(main);
    }
    
    public final void update(ControlData cdata, ViewData vdata) {
        String modelname = ((DataItem)vdata.getElement("modelname")).getValue();
        
        cdata.setReloadableView(true);
        cdata.addParameter("mode", "update");
        cdata.addParameter("modelname", modelname);
        cdata.redirect(null, "structure");
    }
}
