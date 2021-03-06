/*
Copyright 2016 JE Bailey

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package explorer.ui.tree;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.commons.mime.MimeTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import explorer.node.NodeTypeUtil;
import explorer.ui.IconCache;
import explorer.ui.IconCache.Type;

@org.apache.felix.scr.annotations.Component(name = "Sling Explorer UI - Tree Node Renderer")
@Service(value = DefaultTreeCellRenderer.class)
@Property(name = "type", value = "updatePane")
public class JcrTreeNodeRenderer extends DefaultTreeCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Reference
	MimeTypeService mimes;

	private static final Logger log = LoggerFactory.getLogger(JcrTreeNodeRenderer.class);

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		Type type = Type.file;
		if (value != null && (value instanceof Resource)) {
			Resource resource = (Resource) value;
			try {
				if (NodeTypeUtil.isType(resource, "nt:folder")) {
					type = expanded ? Type.folder_open : Type.folder;
				} else if (NodeTypeUtil.isType(resource, "nt:file")) {
					String prop = mimeType(resource);
					if (prop != null) {

						String[] mime = prop.split("/");
						if (mime.length == 2) {
							type = getType(mime[1]);
							if (type == null) {
								type = getType(mime[0]);
							}
						}
					}
				} else {
					if (resource.getPath().equals("/")) {
						type = Type.db;
					}
					type = Type.node_select_child;
				}
			} catch (Exception e) {
				log.error(e.toString());
			}
		}
		setIcon(IconCache.getIcon(type));
		return this;
	}

	private String mimeType(Resource resource) {
		ResourceMetadata metaData = resource.getResourceMetadata();
		String prop = metaData.getContentType();
		if (prop == null) {
			prop = mimes.getMimeType(resource.getName());
		}
		if (prop == null) {
			prop = "";
		}
		return prop;
	}

	private Type getType(String value) {
		try {
			return Type.valueOf(value);
		} catch (Exception e) {
			return null;
		}
	}

}
