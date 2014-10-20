package explorer.ui.tree.actions;

import static org.osgi.service.event.EventConstants.EVENT_TOPIC;

import java.awt.event.ActionEvent;

import javax.jcr.RepositoryException;
import javax.swing.AbstractAction;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import explorer.core.api.ResourceTreeModel;
import explorer.core.api.SessionProvider;
import explorer.ui.EventTypes;
import explorer.ui.ResourceClipboardBuffer;
import explorer.ui.contentview.TabContainer;

@SuppressWarnings("serial")
@Component(name = "Sling Explorer Menu Action - Paste Node", description = "Pastes the Resource from clipboard")
@Service(value = { AbstractAction.class, EventHandler.class })
@Properties(value = { @Property(name = EVENT_TOPIC, value = EventTypes.VIEW_SELECTION),
		@Property(name = "menuType", value = "TREEMENU") })
public class Paste extends AbstractAction implements EventHandler {

	private Resource selectedResource;

	@Reference
	private TabContainer editorTab;
	
	@Reference
	private ResourceTreeModel treeModel;
	
	@Reference
	SessionProvider sessionProvider;
	
	@Reference
	private ResourceClipboardBuffer buffer;

	public Paste() {
		super("Paste");
	}
	
	private static final Logger log = LoggerFactory.getLogger(Paste.class);
	
	@Override
	public void actionPerformed(ActionEvent event) {
		try {
			Resource source = buffer.getResource();
			String destination = selectedResource.getPath() + "/" + source.getName();
			if (buffer.isMove()){
				sessionProvider.move(source.getPath(), destination);
				sessionProvider.save();
				treeModel.fireStructureChanged(source.getParent());
			} else {
				sessionProvider.getWorkspace().copy(source.getPath(), destination);
			}
			treeModel.fireStructureChanged(selectedResource);
			buffer.clear();
		} catch (RepositoryException e) {
			log.error(e.getLocalizedMessage());
		}


	}

	@Override
	public void handleEvent(Event event) {
		selectedResource = (Resource) event.getProperty("data");
		setEnabled(buffer.hasResource());
	}
}
