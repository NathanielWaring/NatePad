import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.MenuBarUI;
import javax.swing.plaf.basic.BasicMenuBarUI;
import javax.swing.text.DefaultEditorKit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.undo.UndoManager;

/**
 * @author Nathaniel Waring
 * 
 *         This is a Notepad type program i wrote, hence the name NatePad. I
 *         wanted to get a better grasp of JComponents, so i figured the best
 *         way to do that is to build a program that uses a lot of them, so i
 *         can see how they interact and whatnot. I ened up redoing the code a
 *         number of times as i found more efficient ways of doing things (or
 *         more specifically, found that someone already wrote the method i was
 *         trying to write, and i just needed to inlude it). It's pretty simple
 *         of a program, but I am pleased with how it turned out and opperates.
 * 
 *         Each action has it's own nested class written for it that extends
 *         AbstractAction, except copy,cut,paste, which extend built in actions
 *         for those functions.
 * 
 *         Dealing with the recent documents list within the file menu was
 *         probablby the most challenging part of the whole program, as it meant
 *         having an object that would persist, that saves the name and File
 *         address of each file in the order they were accessed. I ended up
 *         creating a seperate class (RecentList) in order to simplify things,
 *         and make an object with both a String array and a File array to store
 *         both pieces of data.
 * 
 * 
 */

public class NatePad extends JFrame {

	private static final long serialVersionUID = 1L;

	private Border panelBorder = new LineBorder((Color.BLACK), 2);
	private JTextArea mainBox;
	private String currentDocument = "Untitled";
	private File currentFile = null;

	private UndoManager undoManager = new UndoManager();
	private RecentList recentList = new RecentList();

	private JFileChooser chooser = new JFileChooser(".\\Documents");

	private FileNameExtensionFilter filter = new FileNameExtensionFilter("*.txt", "txt");
	private ImageIcon icon = new ImageIcon("data\\NatePad.jpg");

	public NatePad() {

		this.setIconImage(icon.getImage());
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setUndecorated(false);
		this.setTitle("NatePad - " + currentDocument);
		this.setVisible(true);
		//this.setBackground(Color.WHITE);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		

		chooser.addChoosableFileFilter(filter);
		chooser.setFileFilter(filter);

		// RECENT MENU //

		try {
			recentList = recentList.loadRecent();

		} catch (IOException e) {
			System.out.println("Loading recent list at startup failed");
			recentList = new RecentList();
			recentList.add("Nate", new File("Documents\\Nate.txt"));
			try {
				recentList.saveRecent();
			} catch (IOException e1) {

				e1.printStackTrace();
			}
		}
		currentDocument = (String) recentList.mostRecent()[0];
		currentFile = (File) recentList.mostRecent()[1];
		Object[][] loadList = recentList.getRecentList();
		JMenu recentMenu = new JMenu("Recent");
		for (int i = 0; i < recentList.size(); i++) {

			recentMenu.add(new JMenuItem(new RecentAction((String) loadList[0][i], (File) loadList[1][i])));
		}

		// MENU ITEMS //

		JMenuItem fileItems[] = { new JMenuItem(new NewAction()), new JMenuItem(new SaveAction()),
				new JMenuItem(new SaveAsAction()), new JMenuItem(new LoadAction()), recentMenu,
				new JMenuItem(new ExitAction()) };
		JMenuItem editItems[] = { new JMenuItem(new UndoAction()), new JMenuItem(new RedoAction()),
				new JMenuItem(new CutAction()), new JMenuItem(new CopyAction()), new JMenuItem(new PasteAction()),
				new JMenuItem(new SelectAllAction()) };
		JMenuItem formatItems[] = { new JCheckBoxMenuItem(new BoldAction()), new JMenuItem(new FontAction()),
				new JMenuItem(new SizeAction()) };
		JMenuItem helpItesm[] = { new JMenuItem(new HelpAction()), new JMenuItem(new AboutAction()) };

		// MAIN MENUS //

		JMenu[] menus = { new JMenu("File"), new JMenu("Edit"), new JMenu("Format"), new JMenu("Help") };
		menus[0].setMnemonic('F');
		menus[1].setMnemonic('E');
		menus[2].setMnemonic('O');
		menus[3].setMnemonic('H');

		menus[0].addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				Object[][] loadList = recentList.getRecentList();
				JMenu recentMenu = new JMenu("Recent");
				for (int i = 0; i < recentList.size(); i++) {

					recentMenu.add(new JMenuItem(new RecentAction((String) loadList[0][i], (File) loadList[1][i])));
				}

				menus[0].remove(4);
				menus[0].insert(recentMenu, 4);
				recentMenu.repaint();
			}
		});
		menus[1].addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (undoManager.canUndo()) {
					editItems[0].setEnabled(true);
				} else {
					editItems[0].setEnabled(false);
				}
				if (undoManager.canRedo()) {
					editItems[1].setEnabled(true);
				} else {
					editItems[1].setEnabled(false);
				}
			}
		});
		for (JMenuItem a : fileItems)
			menus[0].add(a);
		for (JMenuItem a : editItems)
			menus[1].add(a);
		for (JMenuItem a : formatItems)
			menus[2].add(a);
		for (JMenuItem a : helpItesm)
			menus[3].add(a);

		JMenuBar menuBar = new JMenuBar();
		for (int i = 0; i < menus.length; i++) {
			menuBar.add(menus[i]);
		}
		
		this.setJMenuBar(menuBar);
		

		// MAIN BOX //
		
		mainBox = new JTextArea(this.getWidth(), this.getHeight());
		mainBox.setVisible(true);
		mainBox.setBorder(panelBorder);
		mainBox.setBackground(Color.WHITE);
		mainBox.setEditable(true);
		mainBox.setLineWrap(true);
		mainBox.setFont(new Font("Monospaced", Font.PLAIN, 16));
		mainBox.setWrapStyleWord(true);
	

		// UNDO/REDO EDIT LISTENER //

		mainBox.getDocument().addUndoableEditListener(new UndoableEditListener() {
			public void undoableEditHappened(UndoableEditEvent e) {
				undoManager.addEdit(e.getEdit());
			}
		});
		JMenuItem mouseItems[] = { new JMenuItem(new UndoAction()), new JMenuItem(new RedoAction()),
				new JMenuItem(new CutAction()), new JMenuItem(new CopyAction()), new JMenuItem(new PasteAction()),
				new JMenuItem(new SelectAllAction()) };
		JPopupMenu rightClick = new JPopupMenu();
		for (JMenuItem a : mouseItems)
			rightClick.add(a);
		mainBox.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					if (undoManager.canUndo()) {
						mouseItems[0].setEnabled(true);
					} else {
						mouseItems[0].setEnabled(false);
					}
					if (undoManager.canRedo()) {
						mouseItems[1].setEnabled(true);
					} else {
						mouseItems[1].setEnabled(false);
					}
					rightClick.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

		JScrollPane mainScrollPane = new JScrollPane(mainBox);
		add(mainScrollPane);

		this.pack();
		mainBox.requestFocus();
	}

	public static void main(String[] args) {

		Boolean DEBUG = false;
		if (!DEBUG)
			new NatePad();
		if (DEBUG) {
			RecentList recentList = new RecentList();
			recentList.add("Nate", new File("Documents\\Nate.txt"));
			try {
				recentList.saveRecent();
			} catch (IOException e) {
				System.out.println("Failed");
				e.printStackTrace();
			}
		}
	}

	/**
	 * NEW ACTION
	 */

	class NewAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public NewAction() {
			super("New", null);
			putValue(SHORT_DESCRIPTION, "This creates a new text file");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
		}

		public void actionPerformed(ActionEvent e) {
			Boolean ready = mainBox.getText().equals("");
			File file, saveFile = null;
			if (!ready) {
				int choise = JOptionPane.showConfirmDialog(null, "Do you want to save the current file?", "Save?",
						JOptionPane.YES_NO_CANCEL_OPTION);
				if (choise == 0) {

					String saveName = currentDocument;
					saveFile = currentFile;

					ready = !saveName.equals("Untitled");
					while (!ready) {
						int choice = chooser.showSaveDialog(null);
						if (choice == JFileChooser.CANCEL_OPTION)
							break;
						ready = (choice == JFileChooser.APPROVE_OPTION);
						if (ready) {
							file = chooser.getSelectedFile().getAbsoluteFile();
							ready = !file.exists();
							saveName = file.getName();
							saveFile = file;
						}

						if (!ready) {

							if (!saveName.equals("Untitled")) {

								int confirm = JOptionPane.showConfirmDialog(null,
										"File exists, do you want to overwrite it?", "Overwrite?",
										JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null);
								if (confirm == 0) {
									ready = true;
								} else if (confirm == 2) {
									break;
								}
							}
						}
					}
					if (ready) {
						String saveText = mainBox.getText();

						try {
							saveDoc(saveFile, new String[] { saveText });
							recentList.add(saveName, saveFile);
							currentDocument = saveName;
							currentFile = saveFile;
							setTitle("NatePad - " + currentDocument);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				} else if (choise == 1) {
					ready = true;
				}
			}
			if (ready) {

				mainBox.selectAll();
				mainBox.replaceSelection("");
				currentDocument = "Untitled";
				setTitle("NatePad - " + currentDocument);
			}
		}
	}

	/**
	 * SAVE ACTION
	 */

	class SaveAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public SaveAction() {
			super("Save", null);
			putValue(SHORT_DESCRIPTION, "This saves the current file");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
		}

		public void actionPerformed(ActionEvent e) {

			String saveText = mainBox.getText();
			String saveName = currentDocument;
			Boolean hasName = !saveName.equals("Untitled");
			Boolean ready = hasName;
			File saveFile = currentFile;

			if (!hasName) {
				int choice = chooser.showSaveDialog(NatePad.this);
				hasName = (choice == JFileChooser.APPROVE_OPTION);
				if (hasName) {
					saveFile = chooser.getSelectedFile();
					saveName = saveFile.getName();
				}

				ready = !saveFile.exists();
				while (!ready) {
					int confirm = JOptionPane.showConfirmDialog(null, "File exists, do you want to overwrite it?",
							"Overwrite?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null);
					if (confirm == 0) {
						ready = true;
					} else if (confirm == 1) {
						ready = (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION);
						saveFile = chooser.getSelectedFile();
						ready = !saveFile.exists();
					} else {
						break;
					}
				}
				saveName = saveFile.getName();
				int period = saveName.indexOf('.');
				if (period > 0) {
					saveName = saveName.substring(0, period);
				}
			}
			if (ready) {

				try {
					saveDoc(saveFile, new String[] { saveText });
					recentList.add(saveName, saveFile);
					currentDocument = saveName;
					currentFile = saveFile;
					setTitle("NatePad - " + currentDocument);

				} catch (IOException e1) {
					System.out.println("Save failed");
					e1.printStackTrace();
				}

			}
		}
	}

	/**
	 * SAVE AS ACTION
	 */

	class SaveAsAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public SaveAsAction() {
			super("Save As", null);
			putValue(SHORT_DESCRIPTION, "This saves the current file with a new name");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_A));

		}

		public void actionPerformed(ActionEvent e) {

			String saveText = "";
			String saveName = currentDocument;
			File saveFile = null;
			saveText = mainBox.getText();
			Boolean ready = false;

			while (!ready) {
				ready = (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION);
				saveFile = chooser.getSelectedFile();
				saveName = saveFile.getName();
				int period = saveName.indexOf('.');
				if (period > 0) {
					saveName = saveName.substring(0, period);
				}
				ready = !saveFile.exists();
				if (!ready) {
					int confirm = JOptionPane.showConfirmDialog(null, "File exists, do you want to overwrite it?",
							"Overwrite?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null);
					if (confirm == 0) {
						ready = true;
					} else if (confirm == 2) {
						break;
					}
				}
			}

			if (ready) {
				try {
					NatePad.saveDoc(saveFile, new String[] { saveText });
					recentList.add(saveName, saveFile);
					currentDocument = saveName;
					setTitle("NatePad - " + currentDocument);

				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}

		}
	}

	/**
	 * LOAD ACTION
	 */

	class LoadAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public LoadAction() {
			super("Load", null);
			putValue(SHORT_DESCRIPTION, "This loads a file");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_L));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK));
		}

		public void actionPerformed(ActionEvent e) {
			String loadText = "";
			String loadName = null;
			File loadFile = null;

			if (chooser.showSaveDialog(NatePad.this) == JFileChooser.APPROVE_OPTION) {
				loadFile = chooser.getSelectedFile().getAbsoluteFile();
				loadName = loadFile.getName();

				int endFile = 0;
				while (loadName.charAt(endFile) != '.' && endFile < loadName.length()) {
					endFile++;
				}
				loadName = loadName.substring(0, endFile);

				try {
					loadText = NatePad.loadDoc(loadFile);
					recentList.add(loadName, loadFile);
					mainBox.selectAll();
					mainBox.replaceSelection(loadText);
					recentList.add(loadName, loadFile);

				} catch (IOException ioe) {
					System.out.println("Load failed");
					// ioe.printStackTrace();
				}
				currentDocument = loadName;
				setTitle("NatePad - " + currentDocument);

			}

		}
	}

	/**
	 * RECENT ACTION
	 */

	class RecentAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		private String recentName;
		private File recentAddress;

		public RecentAction(String name, File address) {
			super(name, null);
			putValue(SHORT_DESCRIPTION, "this will load " + name);
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
			this.recentName = name;
			this.recentAddress = address;

		}

		public void actionPerformed(ActionEvent e) {

			String loadText = "";

			try {
				loadText = NatePad.loadDoc(recentAddress);

				mainBox.selectAll();
				mainBox.replaceSelection(loadText);
				recentList.add(recentName, recentAddress);

				currentDocument = recentName;
				setTitle("NatePad - " + currentDocument);
			} catch (FileNotFoundException fnfe) {
				recentList.remove(recentName, recentAddress);
				JOptionPane.showMessageDialog(null, "I'm sorry, that file cannot be found", "",
						JOptionPane.ERROR_MESSAGE);

			}

		}
	}

	/**
	 * EXIT ACTION
	 */

	class ExitAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public ExitAction() {
			super("Exit", null);
			putValue(SHORT_DESCRIPTION, "This exits the system");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_X));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK));
		}

		public void actionPerformed(ActionEvent e) {
			int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?", "Quit?",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null);
			if (confirm == 0) {
				try {
					recentList.saveRecent();
				} catch (IOException e1) {
					System.out.println("Couldn't save recentList");
				}
				System.exit(1);
			}
		}
	}

	/**
	 * UNDO ACTION
	 */

	class UndoAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public UndoAction() {
			super("Undo", null);
			putValue(SHORT_DESCRIPTION, "This undoes the last action");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_Z));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));

		}

		public void actionPerformed(ActionEvent e) {

			if (undoManager.canUndo()) {
				undoManager.undo();
			}
		}
	}

	/**
	 * REDO ACTION
	 */

	class RedoAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public RedoAction() {
			super("Redo", null);
			putValue(SHORT_DESCRIPTION, "This redoes a previously undone action");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_Y));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK));

		}

		public void actionPerformed(ActionEvent e) {

			if (undoManager.canRedo()) {
				undoManager.redo();
			}

		}

	}

	/**
	 * CUT ACTION
	 */

	class CutAction extends DefaultEditorKit.CutAction {

		private static final long serialVersionUID = 1L;

		public CutAction() {
			super();
			putValue(NAME, "Cut");
			putValue(SHORT_DESCRIPTION, "This will cut the select text");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK));

		}

	}

	/**
	 * COPY ACTION
	 */

	class CopyAction extends DefaultEditorKit.CopyAction {

		private static final long serialVersionUID = 1L;

		public CopyAction() {
			super();
			putValue(NAME, "Copy");
			putValue(SHORT_DESCRIPTION, "This will copy the selected text");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK));
		}

	}

	/**
	 * PASTE ACTION
	 */

	class PasteAction extends DefaultEditorKit.PasteAction {

		private static final long serialVersionUID = 1L;

		public PasteAction() {
			super();
			putValue(NAME, "Paste");
			putValue(SHORT_DESCRIPTION, "This paste the text in the clipboard");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK));

		}

	}

	/**
	 * SELECT ALL ACTION
	 */

	class SelectAllAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public SelectAllAction() {
			super("Select All", null);
			putValue(SHORT_DESCRIPTION, "This will select all the text");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK));
		}

		public void actionPerformed(ActionEvent e) {

			mainBox.selectAll();

		}
	}

	/**
	 * BOLD ACTION
	 */

	class BoldAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public BoldAction() {
			super("Bold", null);
			putValue(SHORT_DESCRIPTION, "This will select all the text");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_B));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK));
			// putValue(SELECTED_KEY,isBold);
		}

		public void actionPerformed(ActionEvent e) {
			Font tempFont = new Font(mainBox.getFont().getName(),
					(!mainBox.getFont().isBold() ? Font.BOLD : Font.PLAIN), mainBox.getFont().getSize());

			mainBox.setFont(tempFont);

		}
	}

	/**
	 * FONT ACTION
	 */

	class FontAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		Object selectedFont;

		public FontAction() {
			super("Font", null);
			putValue(SHORT_DESCRIPTION, "This will show available font options");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_F));
		}

		public void actionPerformed(ActionEvent e) {
			Object fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
			selectedFont = (JOptionPane.showInputDialog(null, "Choose a font", "Font", JOptionPane.OK_CANCEL_OPTION,
					null, fonts, mainBox.getFont().getName()));
			if (selectedFont != null) {
				Font tempFont = new Font((String) selectedFont, (mainBox.getFont().isBold() ? Font.BOLD : Font.PLAIN),
						mainBox.getFont().getSize());

				mainBox.setFont(tempFont);
			}
		}
	}

	/**
	 * SIZE ACTION
	 */

	class SizeAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		Object selectedSize;

		public SizeAction() {
			super("Size", null);
			putValue(SHORT_DESCRIPTION, "This will let you change the font size");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));

		}

		public void actionPerformed(ActionEvent e) {
			Object sizes[] = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30 };
			selectedSize = JOptionPane.showInputDialog(null, "Choose a font size", "Size", JOptionPane.OK_CANCEL_OPTION,
					null, sizes, mainBox.getFont().getSize());

			if (selectedSize != null) {
				Font tempFont = new Font(mainBox.getFont().getFontName(),
						(mainBox.getFont().isBold() ? Font.BOLD : Font.PLAIN), (int) selectedSize);

				mainBox.setFont(tempFont);
			}
		}
	}

	/**
	 * HELP ACTION
	 */

	class HelpAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		Object selectedSize;

		public HelpAction() {
			super("Help", null);
			putValue(SHORT_DESCRIPTION, "This is the help menu");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_H));

		}

		public void actionPerformed(ActionEvent e) {

			String helpText = "You're on your own dude. \n            - Sorry -";
			JOptionPane.showMessageDialog(null, helpText, "Help", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * ABOUT ACTION
	 */
	class AboutAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		Object selectedSize;

		public AboutAction() {
			super("About", null);
			putValue(SHORT_DESCRIPTION, "This gives you information about NatePad");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_A));

		}

		public void actionPerformed(ActionEvent e) {

			String aboutText = "NatePad was designed by Nathaniel Waring in an attempt to familiarize himself"
					+ "\nwith building JMenus, JTextAreas, JFrames and other related components."
					+ "\nPlease feel free to use it in any way you want, and if you're interested in the sourcecode,"
					+ "\nhave suggestions about improvements, or want to offer him a lucrative job programming in Java (*Cough Cough*),"
					+ "\nplease contact him at Nathaniel.g.waring@gmail.com.  Cheers!";
			JOptionPane.showMessageDialog(null, aboutText, "About NatePad", JOptionPane.INFORMATION_MESSAGE);
		}

	}
	

	
	
	

	/**
	 * SAVE DOC METHOD
	 * 
	 * @param theFile
	 * @param text
	 * @throws IOException
	 */
	public static void saveDoc(File theFile, String[] text) throws IOException {
		File file = theFile;
		if (!file.getName().endsWith(".txt")) {
			String saveName = file.getAbsolutePath();
			if (saveName.indexOf('.') > 0) {
				saveName = saveName.substring(0, saveName.indexOf('.'));
			}
			file = new File(saveName + ".txt");
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		for (int i = 0; i < text.length; i++) {
			writer.write(text[i]);
			writer.newLine();
		}
		writer.flush();
		writer.close();
	}

	/**
	 * LOAD DOC METHOD
	 * 
	 * @param file
	 * @return loadText
	 * @throws FileNotFoundException
	 */
	public static String loadDoc(File file) throws FileNotFoundException {
		Scanner inFile;
		inFile = new Scanner(file);
		String loadText = "";
		Boolean hasNextLine = true;
		while (hasNextLine) {
			loadText += inFile.nextLine() + "\n";
			hasNextLine = inFile.hasNext();
		}
		inFile.close();
		return loadText;
	}
}
