package de.jutzig.jabylon.ui.components;

import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;

import de.jutzig.jabylon.properties.PropertyFileDescriptor;
import de.jutzig.jabylon.ui.breadcrumb.BreadCrumb;
import de.jutzig.jabylon.ui.breadcrumb.CrumbTrail;
import de.jutzig.jabylon.ui.container.PropertyPairContainer;
import de.jutzig.jabylon.ui.container.PropertyPairContainer.PropertyPairItem;

public class PropertiesEditor extends GridLayout implements CrumbTrail, ItemClickListener {

	private PropertyFileDescriptor descriptor;
	private TextArea orignal;
	private TextArea translated;
	private Label keyLabel;

	public PropertiesEditor(PropertyFileDescriptor descriptor) {
		this.descriptor = descriptor;
		setColumns(2);
		setRows(3);
		createContents();
		setSpacing(true);
		setMargin(true);
	}
	
	
	
	
	
	private void createContents() {
		Table table = new Table();
		PropertyPairContainer propertyPairContainer = new PropertyPairContainer(descriptor);
		table.setContainerDataSource(propertyPairContainer);
		table.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_EXPLICIT);
		table.setVisibleColumns(propertyPairContainer.getContainerPropertyIds().toArray());
		table.setColumnHeaders(new String[]{"Original","Translation"});
		table.setEditable(false);
		table.setWriteThrough(false);
		table.setWidth(100, UNITS_PERCENTAGE);
		
		table.setSelectable(true);
        table.setMultiSelect(true);
        table.setImmediate(true); // react at once when something is selected
        table.addListener(this);
        addComponent(table,0,0,1,1);
        
		
        createEditorArea();
	}





	private void createEditorArea() {
		keyLabel = new Label();
		keyLabel.setValue("No Selection");
		addComponent(keyLabel,0,2,1,2);
		
		orignal = new TextArea();
		orignal.setWidth(400, UNITS_PIXELS);
		orignal.setEnabled(false);
		addComponent(orignal);
		translated = new TextArea();
		translated.setWidth(400, UNITS_PIXELS);
		
		addComponent(translated);
		
		
	}





	@Override
	public CrumbTrail walkTo(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public String getTrailCaption() {
		return descriptor.getLocation().toString();
	}





	@Override
	public void itemClick(ItemClickEvent event) {
		Item theItem = event.getItem();
		PropertyPairItem item = (PropertyPairItem) theItem;
		item.getSourceProperty();
		
		keyLabel.setValue(item.getSourceProperty().getKey());
//		orignal.setValue(item.getSourceProperty().getValue());
//		
		translated.setPropertyDataSource(item.getTarget());
		translated.setWriteThrough(true);
		orignal.setPropertyDataSource(item.getSource());
		
	}



}
