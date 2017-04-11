/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mobac.gui.dialogs;

import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mobac.utilities.GBCTable;

public class FontChooser {

	private static final String FONT_NAMES[] = GraphicsEnvironment.getLocalGraphicsEnvironment()
			.getAvailableFontFamilyNames();
	private static final String STYLES[] = new String[] { "Plain", "Bold", "Italic", "Bold + Italic" };

	public static final Font DEFAULT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

	public static String encodeFont(Font font) {
		String style;
		switch (font.getStyle()) {
		case Font.PLAIN:
			style = "PLAIN";
			break;
		case Font.BOLD:
			style = "BOLD";
			break;
		case Font.ITALIC:
			style = "ITALIC";
			break;
		case Font.BOLD | Font.ITALIC:
			style = "BOLDITALIC";
			break;
		default:
			style = "PLAIN";
		}
		return font.getName() + "-" + style + "-" + font.getSize();
	}

	private static JScrollPane scroll(JList jList, String title) {
		JLabel jLabel = new JLabel(title);
		jLabel.setHorizontalAlignment(JLabel.CENTER);
		JScrollPane jScrollPane = new JScrollPane(jList);
		jScrollPane.setColumnHeaderView(jLabel);
		return jScrollPane;
	}

	private final JDialog jDialog = new JDialog();

	private final JLabel jLabelPreview = new JLabel("DUMMY");

	private final JList jListName = createJList(FONT_NAMES),
			jListStyle = createJList(STYLES),
			jListSize = createJList(new Integer[] { 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24 });

	private final JButton jButtonOK = new JButton("OK"), jButtonCancel = new JButton("Cancel");

	private boolean wasCanceled;

	public FontChooser() {
		jDialog.setTitle("Choose font");
		jDialog.setModalityType(ModalityType.APPLICATION_MODAL);

		jLabelPreview.setHorizontalAlignment(JLabel.CENTER);
		jLabelPreview.setVerticalAlignment(JLabel.CENTER);
		jLabelPreview.setBorder(BorderFactory.createTitledBorder("Preview"));

		jButtonOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				wasCanceled = false;
				jDialog.setVisible(false);
			}
		});

		jButtonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jDialog.setVisible(false);
			}
		});

		JPanel buttonPane = new JPanel();
		buttonPane.add(jButtonOK);
		buttonPane.add(jButtonCancel);

		JPanel jPanel = new JPanel(new GridBagLayout());
		jPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		GBCTable gbc = new GBCTable();
		jPanel.add(scroll(jListName, "Name"), gbc.begin().fill());
		jPanel.add(scroll(jListStyle, "Style"), gbc.incX().fill());
		jPanel.add(scroll(jListSize, "Size"), gbc.incX().fill());
		jPanel.add(jLabelPreview, gbc.begin(1, 2).fillH().gridwidth(3));
		jPanel.add(buttonPane, gbc.incY().fillH().gridwidth(3));

		jDialog.setContentPane(jPanel);
		jDialog.setSize(384, 384);
		jDialog.setMinimumSize(jDialog.getSize());
		setFont(DEFAULT);
	}

	private JList createJList(Object[] objects) {
		JList jList = new JList(objects);
		jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					Font font = getFont();
					jLabelPreview.setFont(font);
					jLabelPreview.setText(encodeFont(font));
				}
			}
		});
		return jList;
	}

	public void setFont(Font font) {
		if (font == null) {
			font = DEFAULT;
		}
		jListName.setSelectedValue(font.getName(), true);
		jListStyle.setSelectedIndex(font.getStyle());
		jListSize.setSelectedValue(font.getSize(), true);
	}

	public Font getFont() {
		String name = (String) jListName.getSelectedValue();
		if (name == null) {
			name = DEFAULT.getName();
		}
		int style = jListStyle.getSelectedIndex();
		if (style == -1) {
			style = DEFAULT.getStyle();
		}
		Integer size = (Integer) jListSize.getSelectedValue();
		if (size == null) {
			size = DEFAULT.getSize();
		}
		return new Font(name, style, size);
	}

	public void show() {
		wasCanceled = true;
		jDialog.setLocationRelativeTo(jDialog.getParent());
		jDialog.setVisible(true);
	}

	public boolean wasCanceled() {
		return wasCanceled;
	}
}
