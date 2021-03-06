/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.widgets;


import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.cairo.*;
import org.eclipse.swt.internal.gtk.*;

/**
 * Instances of this class represent the "windows"
 * which the desktop or "window manager" is managing.
 * Instances that do not have a parent (that is, they
 * are built using the constructor, which takes a
 * <code>Display</code> as the argument) are described
 * as <em>top level</em> shells. Instances that do have
 * a parent are described as <em>secondary</em> or
 * <em>dialog</em> shells.
 * <p>
 * Instances are always displayed in one of the maximized,
 * minimized or normal states:
 * <ul>
 * <li>
 * When an instance is marked as <em>maximized</em>, the
 * window manager will typically resize it to fill the
 * entire visible area of the display, and the instance
 * is usually put in a state where it can not be resized
 * (even if it has style <code>RESIZE</code>) until it is
 * no longer maximized.
 * </li><li>
 * When an instance is in the <em>normal</em> state (neither
 * maximized or minimized), its appearance is controlled by
 * the style constants which were specified when it was created
 * and the restrictions of the window manager (see below).
 * </li><li>
 * When an instance has been marked as <em>minimized</em>,
 * its contents (client area) will usually not be visible,
 * and depending on the window manager, it may be
 * "iconified" (that is, replaced on the desktop by a small
 * simplified representation of itself), relocated to a
 * distinguished area of the screen, or hidden. Combinations
 * of these changes are also possible.
 * </li>
 * </ul>
 * </p><p>
 * The <em>modality</em> of an instance may be specified using
 * style bits. The modality style bits are used to determine
 * whether input is blocked for other shells on the display.
 * The <code>PRIMARY_MODAL</code> style allows an instance to block
 * input to its parent. The <code>APPLICATION_MODAL</code> style
 * allows an instance to block input to every other shell in the
 * display. The <code>SYSTEM_MODAL</code> style allows an instance
 * to block input to all shells, including shells belonging to
 * different applications.
 * </p><p>
 * Note: The styles supported by this class are treated
 * as <em>HINT</em>s, since the window manager for the
 * desktop on which the instance is visible has ultimate
 * control over the appearance and behavior of decorations
 * and modality. For example, some window managers only
 * support resizable windows and will always assume the
 * RESIZE style, even if it is not set. In addition, if a
 * modality style is not supported, it is "upgraded" to a
 * more restrictive modality style that is supported. For
 * example, if <code>PRIMARY_MODAL</code> is not supported,
 * it would be upgraded to <code>APPLICATION_MODAL</code>.
 * A modality style may also be "downgraded" to a less
 * restrictive style. For example, most operating systems
 * no longer support <code>SYSTEM_MODAL</code> because
 * it can freeze up the desktop, so this is typically
 * downgraded to <code>APPLICATION_MODAL</code>.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>BORDER, CLOSE, MIN, MAX, NO_MOVE, NO_TRIM, RESIZE, TITLE, ON_TOP, TOOL, SHEET</dd>
 * <dd>APPLICATION_MODAL, MODELESS, PRIMARY_MODAL, SYSTEM_MODAL</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Activate, Close, Deactivate, Deiconify, Iconify</dd>
 * </dl>
 * Class <code>SWT</code> provides two "convenience constants"
 * for the most commonly required style combinations:
 * <dl>
 * <dt><code>SHELL_TRIM</code></dt>
 * <dd>
 * the result of combining the constants which are required
 * to produce a typical application top level shell: (that
 * is, <code>CLOSE | TITLE | MIN | MAX | RESIZE</code>)
 * </dd>
 * <dt><code>DIALOG_TRIM</code></dt>
 * <dd>
 * the result of combining the constants which are required
 * to produce a typical application dialog shell: (that
 * is, <code>TITLE | CLOSE | BORDER</code>)
 * </dd>
 * </dl>
 * </p>
 * <p>
 * Note: Only one of the styles APPLICATION_MODAL, MODELESS,
 * PRIMARY_MODAL and SYSTEM_MODAL may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see Decorations
 * @see SWT
 * @see <a href="http://www.eclipse.org/swt/snippets/#shell">Shell snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class Shell extends Decorations {
	long /*int*/ shellHandle, tooltipsHandle, tooltipWindow, group, modalGroup;
	boolean mapped, moved, resized, opened, fullScreen, showWithParent, modified, center;
	int oldX, oldY, oldWidth, oldHeight;
	int minWidth, minHeight;
	Control lastActive;
	ToolTip [] toolTips;
	boolean ignoreFocusOut;
	Region originalRegion;

	static final int MAXIMUM_TRIM = 128;
	static final int BORDER = 3;

	static Callback gdkSeatGrabCallback; // Wayland only.

/**
 * Constructs a new instance of this class. This is equivalent
 * to calling <code>Shell((Display) null)</code>.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
 *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
 * </ul>
 */
public Shell () {
	this ((Display) null);
}
/**
 * Constructs a new instance of this class given only the style
 * value describing its behavior and appearance. This is equivalent
 * to calling <code>Shell((Display) null, style)</code>.
 * <p>
 * The style value is either one of the style constants defined in
 * class <code>SWT</code> which is applicable to instances of this
 * class, or must be built by <em>bitwise OR</em>'ing together
 * (that is, using the <code>int</code> "|" operator) two or more
 * of those <code>SWT</code> style constants. The class description
 * lists the style constants that are applicable to the class.
 * Style bits are also inherited from superclasses.
 * </p>
 *
 * @param style the style of control to construct
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
 *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
 * </ul>
 *
 * @see SWT#BORDER
 * @see SWT#CLOSE
 * @see SWT#MIN
 * @see SWT#MAX
 * @see SWT#RESIZE
 * @see SWT#TITLE
 * @see SWT#TOOL
 * @see SWT#NO_TRIM
 * @see SWT#NO_MOVE
 * @see SWT#SHELL_TRIM
 * @see SWT#DIALOG_TRIM
 * @see SWT#ON_TOP
 * @see SWT#MODELESS
 * @see SWT#PRIMARY_MODAL
 * @see SWT#APPLICATION_MODAL
 * @see SWT#SYSTEM_MODAL
 * @see SWT#SHEET
 */
public Shell (int style) {
	this ((Display) null, style);
}

/**
 * Constructs a new instance of this class given only the display
 * to create it on. It is created with style <code>SWT.SHELL_TRIM</code>.
 * <p>
 * Note: Currently, null can be passed in for the display argument.
 * This has the effect of creating the shell on the currently active
 * display if there is one. If there is no current display, the
 * shell is created on a "default" display. <b>Passing in null as
 * the display argument is not considered to be good coding style,
 * and may not be supported in a future release of SWT.</b>
 * </p>
 *
 * @param display the display to create the shell on
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
 *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
 * </ul>
 */
public Shell (Display display) {
	this (display, SWT.SHELL_TRIM);
}
/**
 * Constructs a new instance of this class given the display
 * to create it on and a style value describing its behavior
 * and appearance.
 * <p>
 * The style value is either one of the style constants defined in
 * class <code>SWT</code> which is applicable to instances of this
 * class, or must be built by <em>bitwise OR</em>'ing together
 * (that is, using the <code>int</code> "|" operator) two or more
 * of those <code>SWT</code> style constants. The class description
 * lists the style constants that are applicable to the class.
 * Style bits are also inherited from superclasses.
 * </p><p>
 * Note: Currently, null can be passed in for the display argument.
 * This has the effect of creating the shell on the currently active
 * display if there is one. If there is no current display, the
 * shell is created on a "default" display. <b>Passing in null as
 * the display argument is not considered to be good coding style,
 * and may not be supported in a future release of SWT.</b>
 * </p>
 *
 * @param display the display to create the shell on
 * @param style the style of control to construct
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
 *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
 * </ul>
 *
 * @see SWT#BORDER
 * @see SWT#CLOSE
 * @see SWT#MIN
 * @see SWT#MAX
 * @see SWT#RESIZE
 * @see SWT#TITLE
 * @see SWT#TOOL
 * @see SWT#NO_TRIM
 * @see SWT#NO_MOVE
 * @see SWT#SHELL_TRIM
 * @see SWT#DIALOG_TRIM
 * @see SWT#ON_TOP
 * @see SWT#MODELESS
 * @see SWT#PRIMARY_MODAL
 * @see SWT#APPLICATION_MODAL
 * @see SWT#SYSTEM_MODAL
 * @see SWT#SHEET
 */
public Shell (Display display, int style) {
	this (display, null, style, 0, false);
}

Shell (Display display, Shell parent, int style, long /*int*/ handle, boolean embedded) {
	super ();
	checkSubclass ();
	if (display == null) display = Display.getCurrent ();
	if (display == null) display = Display.getDefault ();
	if (!display.isValidThread ()) {
		error (SWT.ERROR_THREAD_INVALID_ACCESS);
	}
	if (parent != null && parent.isDisposed ()) {
		error (SWT.ERROR_INVALID_ARGUMENT);
	}
	this.center = parent != null && (style & SWT.SHEET) != 0;
	this.style = checkStyle (parent, style);
	this.parent = parent;
	this.display = display;
	if (handle != 0) {
		if (embedded) {
			this.handle = handle;
		} else {
			shellHandle = handle;
			state |= FOREIGN_HANDLE;
		}
	}
	reskinWidget();
	createWidget (0);
}

/**
 * Constructs a new instance of this class given only its
 * parent. It is created with style <code>SWT.DIALOG_TRIM</code>.
 * <p>
 * Note: Currently, null can be passed in for the parent.
 * This has the effect of creating the shell on the currently active
 * display if there is one. If there is no current display, the
 * shell is created on a "default" display. <b>Passing in null as
 * the parent is not considered to be good coding style,
 * and may not be supported in a future release of SWT.</b>
 * </p>
 *
 * @param parent a shell which will be the parent of the new instance
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if the parent is disposed</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
 *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
 * </ul>
 */
public Shell (Shell parent) {
	this (parent, SWT.DIALOG_TRIM);
}

/**
 * Constructs a new instance of this class given its parent
 * and a style value describing its behavior and appearance.
 * <p>
 * The style value is either one of the style constants defined in
 * class <code>SWT</code> which is applicable to instances of this
 * class, or must be built by <em>bitwise OR</em>'ing together
 * (that is, using the <code>int</code> "|" operator) two or more
 * of those <code>SWT</code> style constants. The class description
 * lists the style constants that are applicable to the class.
 * Style bits are also inherited from superclasses.
 * </p><p>
 * Note: Currently, null can be passed in for the parent.
 * This has the effect of creating the shell on the currently active
 * display if there is one. If there is no current display, the
 * shell is created on a "default" display. <b>Passing in null as
 * the parent is not considered to be good coding style,
 * and may not be supported in a future release of SWT.</b>
 * </p>
 *
 * @param parent a shell which will be the parent of the new instance
 * @param style the style of control to construct
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if the parent is disposed</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
 *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
 * </ul>
 *
 * @see SWT#BORDER
 * @see SWT#CLOSE
 * @see SWT#MIN
 * @see SWT#MAX
 * @see SWT#RESIZE
 * @see SWT#TITLE
 * @see SWT#NO_TRIM
 * @see SWT#NO_MOVE
 * @see SWT#SHELL_TRIM
 * @see SWT#DIALOG_TRIM
 * @see SWT#ON_TOP
 * @see SWT#TOOL
 * @see SWT#MODELESS
 * @see SWT#PRIMARY_MODAL
 * @see SWT#APPLICATION_MODAL
 * @see SWT#SYSTEM_MODAL
 * @see SWT#SHEET
 */
public Shell (Shell parent, int style) {
	this (parent != null ? parent.display : null, parent, style, 0, false);
}

/**
 * Invokes platform specific functionality to allocate a new shell
 * that is embedded.
 * <p>
 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
 * API for <code>Shell</code>. It is marked public only so that it
 * can be shared within the packages provided by SWT. It is not
 * available on all platforms, and should never be called from
 * application code.
 * </p>
 *
 * @param display the display for the shell
 * @param handle the handle for the shell
 * @return a new shell object containing the specified display and handle
 *
 * @noreference This method is not intended to be referenced by clients.
 */
public static Shell gtk_new (Display display, long /*int*/ handle) {
	return new Shell (display, null, SWT.NO_TRIM, handle, true);
}

/**
 * Invokes platform specific functionality to allocate a new shell
 * that is not embedded.
 * <p>
 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
 * API for <code>Shell</code>. It is marked public only so that it
 * can be shared within the packages provided by SWT. It is not
 * available on all platforms, and should never be called from
 * application code.
 * </p>
 *
 * @param display the display for the shell
 * @param handle the handle for the shell
 * @return a new shell object containing the specified display and handle
 *
 * @noreference This method is not intended to be referenced by clients.
 *
 * @since 3.3
 */
public static Shell internal_new (Display display, long /*int*/ handle) {
	return new Shell (display, null, SWT.NO_TRIM, handle, false);
}

static int checkStyle (Shell parent, int style) {
	style = Decorations.checkStyle (style);
	style &= ~SWT.TRANSPARENT;
	if ((style & SWT.ON_TOP) != 0) style &= ~(SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.MAX);
	int mask = SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL | SWT.PRIMARY_MODAL;
	if ((style & SWT.SHEET) != 0) {
		style &= ~SWT.SHEET;
		style |= parent == null ? SWT.SHELL_TRIM : SWT.DIALOG_TRIM;
		if ((style & mask) == 0) {
			style |= parent == null ? SWT.APPLICATION_MODAL : SWT.PRIMARY_MODAL;
		}
	}
	int bits = style & ~mask;
	if ((style & SWT.SYSTEM_MODAL) != 0) return bits | SWT.SYSTEM_MODAL;
	if ((style & SWT.APPLICATION_MODAL) != 0) return bits | SWT.APPLICATION_MODAL;
	if ((style & SWT.PRIMARY_MODAL) != 0) return bits | SWT.PRIMARY_MODAL;
	return bits;
}

/**
 * Adds the listener to the collection of listeners who will
 * be notified when operations are performed on the receiver,
 * by sending the listener one of the messages defined in the
 * <code>ShellListener</code> interface.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see ShellListener
 * @see #removeShellListener
 */
public void addShellListener (ShellListener listener) {
	checkWidget();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.Close,typedListener);
	addListener (SWT.Iconify,typedListener);
	addListener (SWT.Deiconify,typedListener);
	addListener (SWT.Activate, typedListener);
	addListener (SWT.Deactivate, typedListener);
}

void addToolTip (ToolTip toolTip) {
	if (toolTips  == null) toolTips = new ToolTip [4];
	for (int i=0; i<toolTips.length; i++) {
		if (toolTips [i] == null) {
			toolTips [i] = toolTip;
			return;
		}
	}
	ToolTip [] newToolTips = new ToolTip [toolTips.length + 4];
	newToolTips [toolTips.length] = toolTip;
	System.arraycopy (toolTips, 0, newToolTips, 0, toolTips.length);
	toolTips = newToolTips;
}

void adjustTrim () {
	if (display.ignoreTrim) return;
	GtkAllocation allocation = new GtkAllocation ();
	GTK.gtk_widget_get_allocation (shellHandle, allocation);
	int width = allocation.width;
	int height = allocation.height;
	long /*int*/ window = gtk_widget_get_window (shellHandle);
	GdkRectangle rect = new GdkRectangle ();
	GDK.gdk_window_get_frame_extents (window, rect);
	int trimWidth = Math.max (0, rect.width - width);
	int trimHeight = Math.max (0, rect.height - height);
	/*
	* Bug in GTK.  gdk_window_get_frame_extents() fails for various window
	* managers, causing a large incorrect value to be returned as the trim.
	* The fix is to ignore the returned trim values if they are too large.
	*
	* Additionally, ignore trim for Shells with SWT.RESIZE and SWT.ON_TOP set.
	* See bug 319612.
	*/
	if (trimWidth > MAXIMUM_TRIM || trimHeight > MAXIMUM_TRIM || isCustomResize()) {
		display.ignoreTrim = true;
		return;
	}
	boolean hasTitle = false, hasResize = false, hasBorder = false;
	if ((style & SWT.NO_TRIM) == 0) {
		hasTitle = (style & (SWT.MIN | SWT.MAX | SWT.TITLE | SWT.MENU)) != 0;
		hasResize = (style & SWT.RESIZE) != 0;
		hasBorder = (style & SWT.BORDER) != 0;
	}
	int trimStyle;
	if (hasTitle) {
		if (hasResize) {
			trimStyle = Display.TRIM_TITLE_RESIZE;
		} else if (hasBorder) {
			trimStyle = Display.TRIM_TITLE_BORDER;
		} else {
			trimStyle = Display.TRIM_TITLE;
		}
	} else if (hasResize) {
		trimStyle = Display.TRIM_RESIZE;
	} else if (hasBorder) {
		trimStyle = Display.TRIM_BORDER;
	} else {
		trimStyle = Display.TRIM_NONE;
	}
	if (GTK.GTK3) {
		/*
		 * The workaround for bug 445900 seems to cause problems for some
		 * users on GTK2, see bug 492695. The fix is to only adjust the
		 * shell size on GTK3.
		 */
		Rectangle bounds = getBoundsInPixels();
		int widthAdjustment = display.trimWidths[trimStyle] - trimWidth;
		int heightAdjustment = display.trimHeights[trimStyle] - trimHeight;
		if (widthAdjustment == 0 && heightAdjustment == 0) return;

		bounds.width += widthAdjustment;
		bounds.height += heightAdjustment;
		oldWidth += widthAdjustment;
		oldHeight += heightAdjustment;
		if (!getMaximized()) {
			resizeBounds (width + widthAdjustment, height + heightAdjustment, false);
		}
	}
	display.trimWidths[trimStyle] = trimWidth;
	display.trimHeights[trimStyle] = trimHeight;
}

void bringToTop (boolean force) {
	if (!GTK.gtk_widget_get_visible (shellHandle)) return;
	Display display = this.display;
	Shell activeShell = display.activeShell;
	if (activeShell == this) return;
	if (!force) {
		if (activeShell == null) return;
		if (!display.activePending) {
			long /*int*/ focusHandle = GTK.gtk_window_get_focus (activeShell.shellHandle);
			if (focusHandle != 0 && !GTK.gtk_widget_has_focus (focusHandle)) return;
		}
	}
	/*
	* Bug in GTK.  When a shell that is not managed by the window
	* manage is given focus, GTK gets stuck in "focus follows pointer"
	* mode when the pointer is within the shell and its parent when
	* the shell is hidden or disposed. The fix is to use XSetInputFocus()
	* to assign focus when ever the active shell has not managed by
	* the window manager.
	*
	* NOTE: This bug is fixed in GTK+ 2.6.8 and above.
	*/
	boolean xFocus = false;
	if (activeShell != null) {
		display.activeShell = null;
		display.activePending = true;
	}
	/*
	* Feature in GTK.  When the shell is an override redirect
	* window, gdk_window_focus() does not give focus to the
	* window.  The fix is to use XSetInputFocus() to force
	* the focus, or gtk_grab_add() for Wayland.
	*/
	long /*int*/ window = gtk_widget_get_window (shellHandle);
	if ((xFocus || (style & SWT.ON_TOP) != 0)) {
		if (OS.isX11()) {
			long /*int*/ gdkDisplay = GDK.gdk_window_get_display(window);
			long /*int*/ xDisplay = GDK.gdk_x11_display_get_xdisplay(gdkDisplay);
			long /*int*/ xWindow;
			if (GTK.GTK3) {
				xWindow = GDK.gdk_x11_window_get_xid (window);
				GDK.gdk_x11_display_error_trap_push(gdkDisplay);
			} else {
				xWindow = GDK.gdk_x11_drawable_get_xid (window);
				GDK.gdk_error_trap_push ();
			}
			/* Use CurrentTime instead of the last event time to ensure that the shell becomes active */
			OS.XSetInputFocus (xDisplay, xWindow, OS.RevertToParent, OS.CurrentTime);
			if (GTK.GTK3) {
				GDK.gdk_x11_display_error_trap_pop_ignored(gdkDisplay);
			} else {
				GDK.gdk_error_trap_pop ();
			}
		} else {
			if (GTK.GTK_VERSION >= OS.VERSION(3, 20, 0)) {
				if (gdkSeatGrabCallback == null) {
					gdkSeatGrabCallback = new Callback(Shell.class, "GdkSeatGrabPrepareFunc", 3); //$NON-NLS-1$
				}
				long /*int*/ gdkSeatGrabPrepareFunc = gdkSeatGrabCallback.getAddress();
				if (gdkSeatGrabPrepareFunc == 0) SWT.error (SWT.ERROR_NO_MORE_CALLBACKS);
				GTK.gtk_grab_add(shellHandle);
				long /*int*/ seat = GDK.gdk_display_get_default_seat(GDK.gdk_window_get_display(window));
				GDK.gdk_seat_grab(seat, window, 1, true, 0, 0, gdkSeatGrabPrepareFunc, shellHandle);
			}
		}
	} else {
		GDK.gdk_window_focus (window, GDK.GDK_CURRENT_TIME);
	}
	display.activeShell = this;
	display.activePending = true;
}

void center () {
	if (parent == null) return;
	Rectangle rect = getBoundsInPixels ();
	Rectangle parentRect = display.mapInPixels (parent, null, parent.getClientAreaInPixels());
	int x = Math.max (parentRect.x, parentRect.x + (parentRect.width - rect.width) / 2);
	int y = Math.max (parentRect.y, parentRect.y + (parentRect.height - rect.height) / 2);
	Rectangle monitorRect = DPIUtil.autoScaleUp(parent.getMonitor ().getClientArea());
	if (x + rect.width > monitorRect.x + monitorRect.width) {
		x = Math.max (monitorRect.x, monitorRect.x + monitorRect.width - rect.width);
	} else {
		x = Math.max (x, monitorRect.x);
	}
	if (y + rect.height > monitorRect.y + monitorRect.height) {
		y = Math.max (monitorRect.y, monitorRect.y + monitorRect.height - rect.height);
	} else {
		y = Math.max (y, monitorRect.y);
	}
	setLocationInPixels (x, y);
}

@Override
void checkBorder () {
	/* Do nothing */
}

@Override
void checkOpen () {
	if (!opened) resized = false;
}

@Override
long /*int*/ childStyle () {
	return 0;
}

/**
 * Requests that the window manager close the receiver in
 * the same way it would be closed when the user clicks on
 * the "close box" or performs some other platform specific
 * key or mouse combination that indicates the window
 * should be removed.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see SWT#Close
 * @see #dispose
 */
public void close () {
	checkWidget ();
	closeWidget ();
}
void closeWidget () {
	Event event = new Event ();
	sendEvent (SWT.Close, event);
	if (event.doit && !isDisposed ()) dispose ();
}

@Override
Rectangle computeTrimInPixels (int x, int y, int width, int height) {
	checkWidget();
	Rectangle trim = super.computeTrimInPixels (x, y, width, height);
	int border = 0;
	if ((style & (SWT.NO_TRIM | SWT.BORDER | SWT.SHELL_TRIM)) == 0 || isCustomResize()) {
		border = GTK.gtk_container_get_border_width (shellHandle);
	}
	int trimWidth = trimWidth (), trimHeight = trimHeight ();
	trim.x -= (trimWidth / 2) + border;
	trim.y -= trimHeight - (trimWidth / 2) + border;
	trim.width += trimWidth + border * 2;
	trim.height += trimHeight + border * 2;
	if (menuBar != null) {
		forceResize ();
		GtkAllocation allocation = new GtkAllocation ();
		GTK.gtk_widget_get_allocation (menuBar.handle, allocation);
		int menuBarHeight = allocation.height;
		trim.y -= menuBarHeight;
		trim.height += menuBarHeight;
	}
	return trim;
}

@Override
void createHandle (int index) {
	state |= HANDLE | CANVAS;
	if (shellHandle == 0) {
		if (handle == 0) {
			int type = GTK.GTK_WINDOW_TOPLEVEL;
			if ((style & SWT.ON_TOP) != 0) type = GTK.GTK_WINDOW_POPUP;
			shellHandle = GTK.gtk_window_new (type);
		} else {
			shellHandle = GTK.gtk_plug_new (handle);
		}
		if (shellHandle == 0) error (SWT.ERROR_NO_HANDLES);
		if (parent != null) {
			GTK.gtk_window_set_transient_for (shellHandle, parent.topHandle ());
			GTK.gtk_window_set_destroy_with_parent (shellHandle, true);
			// if child shells are minimizable, we want them to have a
			// taskbar icon, so they can be unminimized
			if ((style & SWT.MIN) == 0) {
				GTK.gtk_window_set_skip_taskbar_hint(shellHandle, true);
			}

			/*
			 * For systems running Metacity, by applying the dialog type hint
			 * to a window only the close button can be placed on the title bar.
			 * The style hints for the minimize and maximize buttons are ignored.
			 * See bug 445456.
			 *
			 */
//			if (!isUndecorated ()) {
//				OS.gtk_window_set_type_hint (shellHandle, OS.GDK_WINDOW_TYPE_HINT_DIALOG);
//			}
		}
		/*
		* Feature in GTK.  The window size must be set when the window
		* is created or it will not be allowed to be resized smaller that the
		* initial size by the user.  The fix is to set the size to zero.
		*/
		if ((style & SWT.RESIZE) != 0) {
			GTK.gtk_widget_set_size_request (shellHandle, 0, 0);
			GTK.gtk_window_set_resizable (shellHandle, true);
		} else {
			GTK.gtk_window_set_resizable (shellHandle, false);
		}
		GTK.gtk_window_set_title (shellHandle, new byte [1]);
		if ((style & (SWT.NO_TRIM | SWT.BORDER | SWT.SHELL_TRIM)) == 0) {
			GTK.gtk_container_set_border_width (shellHandle, 1);
			if (GTK.GTK3) {
				if (GTK.GTK_VERSION < OS.VERSION (3, 14, 0)) {
					GTK.gtk_widget_override_background_color (shellHandle, GTK.GTK_STATE_FLAG_NORMAL, new GdkRGBA());
				}
			} else {
				GdkColor color = new GdkColor ();
				GTK.gtk_style_get_black (GTK.gtk_widget_get_style (shellHandle), color);
				GTK.gtk_widget_modify_bg (shellHandle, GTK.GTK_STATE_NORMAL, color);
			}
		}
		if ((style & SWT.TOOL) != 0) {
			GTK.gtk_window_set_type_hint(shellHandle, GDK.GDK_WINDOW_TYPE_HINT_UTILITY);
		}
		if ((style & SWT.NO_TRIM) != 0 ) {
			GTK.gtk_window_set_decorated(shellHandle, false);
		}
		if (isCustomResize ()) {
			GTK.gtk_container_set_border_width (shellHandle, BORDER);
		}
	}
	vboxHandle = gtk_box_new (GTK.GTK_ORIENTATION_VERTICAL, false, 0);
	if (vboxHandle == 0) error (SWT.ERROR_NO_HANDLES);
	createHandle (index, false, true);
	GTK.gtk_container_add (vboxHandle, scrolledHandle);
	GTK.gtk_box_set_child_packing (vboxHandle, scrolledHandle, true, true, 0, GTK.GTK_PACK_END);
	group = GTK.gtk_window_group_new ();
	if (group == 0) error (SWT.ERROR_NO_HANDLES);
	/*
	* Feature in GTK.  Realizing the shell triggers a size allocate event,
	* which may be confused for a resize event from the window manager if
	* received too late.  The fix is to realize the window during creation
	* to avoid confusion.
	*/
	GTK.gtk_widget_realize (shellHandle);
}

@Override
long /*int*/ filterProc (long /*int*/ xEvent, long /*int*/ gdkEvent, long /*int*/ data2) {
	if (!OS.isX11()) return 0;
	int eventType = OS.X_EVENT_TYPE (xEvent);
	if (eventType != OS.FocusOut && eventType != OS.FocusIn) return 0;
	XFocusChangeEvent xFocusEvent = new XFocusChangeEvent();
	OS.memmove (xFocusEvent, xEvent, XFocusChangeEvent.sizeof);
	switch (eventType) {
		case OS.FocusIn:
			if (xFocusEvent.mode == OS.NotifyNormal || xFocusEvent.mode == OS.NotifyWhileGrabbed) {
				switch (xFocusEvent.detail) {
					case OS.NotifyNonlinear:
					case OS.NotifyNonlinearVirtual:
					case OS.NotifyAncestor:
						display.activeShell = this;
						display.activePending = false;
						sendEvent (SWT.Activate);
						if (isDisposed ()) return 0;
						if (isCustomResize ()) {
							GDK.gdk_window_invalidate_rect (gtk_widget_get_window (shellHandle), null, false);
						}
						break;
				}
			}
			break;
		case OS.FocusOut:
			if (xFocusEvent.mode == OS.NotifyNormal || xFocusEvent.mode == OS.NotifyWhileGrabbed) {
				switch (xFocusEvent.detail) {
					case OS.NotifyNonlinear:
					case OS.NotifyNonlinearVirtual:
					case OS.NotifyVirtual:
						Display display = this.display;
						sendEvent (SWT.Deactivate);
						setActiveControl (null);
						if (display.activeShell == this) {
							display.activeShell = null;
							display.activePending = false;
						}
						if (isDisposed ()) return 0;
						if (isCustomResize ()) {
							GDK.gdk_window_invalidate_rect (gtk_widget_get_window (shellHandle), null, false);
						}
						break;
				}
			}
			break;
	}
	return 0;
}

@Override
Control findBackgroundControl () {
	return (state & BACKGROUND) != 0 || backgroundImage != null ? this : null;
}

@Override
Composite findDeferredControl () {
	return layoutCount > 0 ? this : null;
}

/**
 * Returns a ToolBar object representing the tool bar that can be shown in the receiver's
 * trim. This will return <code>null</code> if the platform does not support tool bars that
 * are not part of the content area of the shell, or if the Shell's style does not support
 * having a tool bar.
 * <p>
 *
 * @return a ToolBar object representing the Shell's tool bar, or <code>null</code>.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @since 3.7
 */
public ToolBar getToolBar() {
	checkWidget ();
	return null;
}

@Override
boolean hasBorder () {
	return false;
}

@Override
void hookEvents () {
	super.hookEvents ();
	OS.g_signal_connect_closure_by_id (shellHandle, display.signalIds [KEY_PRESS_EVENT], 0, display.getClosure (KEY_PRESS_EVENT), false);
	OS.g_signal_connect_closure_by_id (shellHandle, display.signalIds [WINDOW_STATE_EVENT], 0, display.getClosure (WINDOW_STATE_EVENT), false);
	OS.g_signal_connect_closure_by_id (shellHandle, display.signalIds [SIZE_ALLOCATE], 0, display.getClosure (SIZE_ALLOCATE), false);
	OS.g_signal_connect_closure_by_id (shellHandle, display.signalIds [CONFIGURE_EVENT], 0, display.getClosure (CONFIGURE_EVENT), false);
	OS.g_signal_connect_closure_by_id (shellHandle, display.signalIds [DELETE_EVENT], 0, display.getClosure (DELETE_EVENT), false);
	OS.g_signal_connect_closure_by_id (shellHandle, display.signalIds [MAP_EVENT], 0, display.shellMapProcClosure, false);
	OS.g_signal_connect_closure_by_id (shellHandle, display.signalIds [ENTER_NOTIFY_EVENT], 0, display.getClosure (ENTER_NOTIFY_EVENT), false);
	OS.g_signal_connect_closure (shellHandle, OS.move_focus, display.getClosure (MOVE_FOCUS), false);
	if (!GTK.GTK3) {
		long /*int*/ window = gtk_widget_get_window (shellHandle);
		GDK.gdk_window_add_filter  (window, display.filterProc, shellHandle);
	} else {
		OS.g_signal_connect_closure_by_id (shellHandle, display.signalIds [FOCUS_IN_EVENT], 0, display.getClosure (FOCUS_IN_EVENT), false);
		OS.g_signal_connect_closure_by_id (shellHandle, display.signalIds [FOCUS_OUT_EVENT], 0, display.getClosure (FOCUS_OUT_EVENT), false);
	}
	if (isCustomResize ()) {
		int mask = GDK.GDK_POINTER_MOTION_MASK | GDK.GDK_BUTTON_RELEASE_MASK | GDK.GDK_BUTTON_PRESS_MASK |  GDK.GDK_ENTER_NOTIFY_MASK | GDK.GDK_LEAVE_NOTIFY_MASK;
		GTK.gtk_widget_add_events (shellHandle, mask);
		OS.g_signal_connect_closure_by_id (shellHandle, display.signalIds [EXPOSE_EVENT], 0, display.getClosure (EXPOSE_EVENT), false);
		OS.g_signal_connect_closure_by_id (shellHandle, display.signalIds [LEAVE_NOTIFY_EVENT], 0, display.getClosure (LEAVE_NOTIFY_EVENT), false);
		OS.g_signal_connect_closure_by_id (shellHandle, display.signalIds [MOTION_NOTIFY_EVENT], 0, display.getClosure (MOTION_NOTIFY_EVENT), false);
		OS.g_signal_connect_closure_by_id (shellHandle, display.signalIds [BUTTON_PRESS_EVENT], 0, display.getClosure (BUTTON_PRESS_EVENT), false);
	}
}

@Override
public boolean isEnabled () {
	checkWidget ();
	return getEnabled ();
}

boolean isUndecorated () {
	return
		(style & (SWT.SHELL_TRIM | SWT.BORDER)) == SWT.NONE ||
		(style & (SWT.NO_TRIM | SWT.ON_TOP)) != 0;
}

/**
 * Determines whether a Shell has both SWT.RESIZE and SWT.ON_TOP set without SWT.NO_TRIM.
 *
 * @return true if this Shell has both SWT.RESIZE and SWT.ON_TOP set without
 * SWT.NO_TRIM, false otherwise.
 */
boolean isCustomResize () {
	return (style & SWT.NO_TRIM) == 0 && (style & (SWT.RESIZE | SWT.ON_TOP)) == (SWT.RESIZE | SWT.ON_TOP);
}

@Override
public boolean isVisible () {
	checkWidget();
	return getVisible ();
}

@Override
void register () {
	super.register ();
	display.addWidget (shellHandle, this);
}

@Override
void releaseParent () {
	/* Do nothing */
}

@Override
public void requestLayout () {
	layout (null, SWT.DEFER);
}

@Override
long /*int*/ topHandle () {
	return shellHandle;
}

void fixActiveShell () {
	if (display.activeShell == this) {
		Shell shell = null;
		if (parent != null && parent.isVisible ()) shell = parent.getShell ();
		if (shell == null && isUndecorated ()) {
			Shell [] shells = display.getShells ();
			for (int i = 0; i < shells.length; i++) {
				if (shells [i] != null && shells [i].isVisible ()) {
					shell = shells [i];
					break;
				}
			}
		}
		if (shell != null) shell.bringToTop (false);
	}
}

void fixShell (Shell newShell, Control control) {
	if (this == newShell) return;
	if (control == lastActive) setActiveControl (null);
	String toolTipText = control.toolTipText;
	if (toolTipText != null) {
		control.setToolTipText (this, null);
		control.setToolTipText (newShell, toolTipText);
	}
}

@Override
long /*int*/ fixedSizeAllocateProc(long /*int*/ widget, long /*int*/ allocationPtr) {
	int clientWidth = 0;
	if ((style & SWT.MIRRORED) != 0) clientWidth = getClientWidth ();
	long /*int*/ result = super.fixedSizeAllocateProc (widget, allocationPtr);
	if ((style & SWT.MIRRORED) != 0) moveChildren (clientWidth);
	return result;
}

@Override
void fixStyle (long /*int*/ handle) {
}

@Override
void forceResize () {
	GtkAllocation allocation = new GtkAllocation ();
	GTK.gtk_widget_get_allocation (vboxHandle, allocation);
	forceResize (allocation.width, allocation.height);
}

void forceResize (int width, int height) {
	int clientWidth = 0;
	if (GTK.GTK3) {
		if ((style & SWT.MIRRORED) != 0) clientWidth = getClientWidth ();
	}
	GtkAllocation allocation = new GtkAllocation ();
	int border = GTK.gtk_container_get_border_width (shellHandle);
	allocation.x = border;
	allocation.y = border;
	allocation.width = width;
	allocation.height = height;
	// Call gtk_widget_get_preferred_size() on GTK 3.20+ to prevent warnings.
	// See bug 486068.
	if (GTK.GTK_VERSION >= OS.VERSION(3, 20, 0)) {
		GtkRequisition minimumSize = new GtkRequisition ();
		GtkRequisition naturalSize = new GtkRequisition ();
		GTK.gtk_widget_get_preferred_size (vboxHandle, minimumSize, naturalSize);
	}
	GTK.gtk_widget_size_allocate (vboxHandle, allocation);
	if (GTK.GTK3) {
		if ((style & SWT.MIRRORED) != 0) moveChildren (clientWidth);
	}
}

/**
 * Returns the receiver's alpha value. The alpha value
 * is between 0 (transparent) and 255 (opaque).
 *
 * @return the alpha value
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @since 3.4
 */
public int getAlpha () {
	checkWidget ();
	if (GTK.gtk_widget_is_composited (shellHandle)) {
		/*
		 * Feature in GTK: gtk_window_get_opacity() is deprecated on GTK3.8
		 * onward. Use gtk_widget_get_opacity() instead.
		 */
		return (int) (GTK.GTK_VERSION > OS.VERSION (3, 8, 0) ? GTK.gtk_widget_get_opacity(shellHandle) * 255 : GTK.gtk_window_get_opacity (shellHandle) * 255);
	}
	return 255;
}

int getResizeMode (double x, double y) {
	GtkAllocation allocation = new GtkAllocation ();
	GTK.gtk_widget_get_allocation (shellHandle, allocation);
	int width = allocation.width;
	int height = allocation.height;
	int border = GTK.gtk_container_get_border_width (shellHandle);
	int mode = 0;
	if (y >= height - border) {
		mode = GDK.GDK_BOTTOM_SIDE ;
		if (x >= width - border - 16) mode = GDK.GDK_BOTTOM_RIGHT_CORNER;
		else if (x <= border + 16) mode = GDK.GDK_BOTTOM_LEFT_CORNER;
	} else if (x >= width - border) {
		mode = GDK.GDK_RIGHT_SIDE;
		if (y >= height - border - 16) mode = GDK.GDK_BOTTOM_RIGHT_CORNER;
		else if (y <= border + 16) mode = GDK.GDK_TOP_RIGHT_CORNER;
	} else if (y <= border) {
		mode = GDK.GDK_TOP_SIDE;
		if (x <= border + 16) mode = GDK.GDK_TOP_LEFT_CORNER;
		else if (x >= width - border - 16) mode = GDK.GDK_TOP_RIGHT_CORNER;
	} else if (x <= border) {
		mode = GDK.GDK_LEFT_SIDE;
		if (y <= border + 16) mode = GDK.GDK_TOP_LEFT_CORNER;
		else if (y >= height - border - 16) mode = GDK.GDK_BOTTOM_LEFT_CORNER;
	}
	return mode;
}

/**
 * Returns <code>true</code> if the receiver is currently
 * in fullscreen state, and false otherwise.
 * <p>
 *
 * @return the fullscreen state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @since 3.4
 */
public boolean getFullScreen () {
	checkWidget();
	return fullScreen;
}

@Override
Point getLocationInPixels () {
	checkWidget ();
	int [] x = new int [1], y = new int [1];
	GTK.gtk_window_get_position (shellHandle, x,y);
	return new Point (x [0], y [0]);
}

@Override
public boolean getMaximized () {
	checkWidget();
	return !fullScreen && super.getMaximized ();
}

/**
 * Returns a point describing the minimum receiver's size. The
 * x coordinate of the result is the minimum width of the receiver.
 * The y coordinate of the result is the minimum height of the
 * receiver.
 *
 * @return the receiver's size
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @since 3.1
 */
public Point getMinimumSize () {
	checkWidget ();
	return DPIUtil.autoScaleDown (getMinimumSizeInPixels ());
}

Point getMinimumSizeInPixels () {
	checkWidget ();
	int width = Math.max (1, minWidth + trimWidth ());
	int height = Math.max (1, minHeight + trimHeight ());
	return new Point (width, height);
}

Shell getModalShell () {
	Shell shell = null;
	Shell [] modalShells = display.modalShells;
	if (modalShells != null) {
		int bits = SWT.APPLICATION_MODAL | SWT.SYSTEM_MODAL;
		int index = modalShells.length;
		while (--index >= 0) {
			Shell modal = modalShells [index];
			if (modal != null) {
				if ((modal.style & bits) != 0) {
					Control control = this;
					while (control != null) {
						if (control == modal) break;
						control = control.parent;
					}
					if (control != modal) return modal;
					break;
				}
				if ((modal.style & SWT.PRIMARY_MODAL) != 0) {
					if (shell == null) shell = getShell ();
					if (modal.parent == shell) return modal;
				}
			}
		}
	}
	return null;
}

/**
 * Gets the receiver's modified state.
 *
 * @return <code>true</code> if the receiver is marked as modified, or <code>false</code> otherwise
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @since 3.5
 */
public boolean getModified () {
	checkWidget ();
	return modified;
}

@Override
Point getSizeInPixels () {
	checkWidget ();
	GtkAllocation allocation = new GtkAllocation ();
	GTK.gtk_widget_get_allocation (vboxHandle, allocation);
	int width = allocation.width;
	int height = allocation.height;
	int border = 0;
	if ((style & (SWT.NO_TRIM | SWT.BORDER | SWT.SHELL_TRIM)) == 0 || isCustomResize()) {
		border = GTK.gtk_container_get_border_width (shellHandle);
	}
	return new Point (width + trimWidth () + 2*border, height + trimHeight () + 2*border);
}

@Override
public boolean getVisible () {
	checkWidget();
	return GTK.gtk_widget_get_visible (shellHandle);
}

/**
 * Returns the region that defines the shape of the shell,
 * or <code>null</code> if the shell has the default shape.
 *
 * @return the region that defines the shape of the shell, or <code>null</code>
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @since 3.0
 *
 */
@Override
public Region getRegion () {
	/* This method is needed for @since 3.0 Javadoc */
	checkWidget ();
	if (originalRegion != null) return originalRegion;
	return region;
}

/**
 * Returns the receiver's input method editor mode. This
 * will be the result of bitwise OR'ing together one or
 * more of the following constants defined in class
 * <code>SWT</code>:
 * <code>NONE</code>, <code>ROMAN</code>, <code>DBCS</code>,
 * <code>PHONETIC</code>, <code>NATIVE</code>, <code>ALPHA</code>.
 *
 * @return the IME mode
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see SWT
 */
public int getImeInputMode () {
	checkWidget();
	return SWT.NONE;
}

@Override
Shell _getShell () {
	return this;
}
/**
 * Returns an array containing all shells which are
 * descendants of the receiver.
 * <p>
 * @return the dialog shells
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Shell [] getShells () {
	checkWidget();
	int count = 0;
	Shell [] shells = display.getShells ();
	for (int i=0; i<shells.length; i++) {
		Control shell = shells [i];
		do {
			shell = shell.getParent ();
		} while (shell != null && shell != this);
		if (shell == this) count++;
	}
	int index = 0;
	Shell [] result = new Shell [count];
	for (int i=0; i<shells.length; i++) {
		Control shell = shells [i];
		do {
			shell = shell.getParent ();
		} while (shell != null && shell != this);
		if (shell == this) {
			result [index++] = shells [i];
		}
	}
	return result;
}

@Override
long /*int*/ gtk_button_press_event (long /*int*/ widget, long /*int*/ event) {
	if (widget == shellHandle) {
		if (isCustomResize ()) {
			if (OS.isX11() && (style & SWT.ON_TOP) != 0 && (style & SWT.NO_FOCUS) == 0) {
				forceActive ();
			}
			GdkEventButton gdkEvent = new GdkEventButton ();
			OS.memmove (gdkEvent, event, GdkEventButton.sizeof);
			if (gdkEvent.button == 1) {
				display.resizeLocationX = gdkEvent.x_root;
				display.resizeLocationY = gdkEvent.y_root;
				int [] x = new int [1], y = new int [1];
				GTK.gtk_window_get_position (shellHandle, x, y);
				display.resizeBoundsX = x [0];
				display.resizeBoundsY = y [0];
				GtkAllocation allocation = new GtkAllocation ();
				GTK.gtk_widget_get_allocation (shellHandle, allocation);
				display.resizeBoundsWidth = allocation.width;
				display.resizeBoundsHeight = allocation.height;
			}
		}
		/**
		 *  Feature in GTK: This handles ungrabbing the keyboard focus from a SWT.ON_TOP window
		 *  if it has editable fields and is running Wayland. Refer to bug 515773.
		 */
		if (!OS.isX11() && GTK.GTK_VERSION >= OS.VERSION(3, 20, 0)) {
			if ((style & SWT.ON_TOP) != 0 && (style & SWT.NO_FOCUS) == 0) {
				GTK.gtk_grab_remove(shellHandle);
				GDK.gdk_seat_ungrab(GDK.gdk_event_get_seat(event));
				GTK.gtk_widget_hide(shellHandle);
			}
		}
		return 0;
	}
	return super.gtk_button_press_event (widget, event);
}

@Override
long /*int*/ gtk_configure_event (long /*int*/ widget, long /*int*/ event) {
	int [] x = new int [1], y = new int [1];
	GTK.gtk_window_get_position (shellHandle, x, y);

	if (!isVisible ()) {
		return 0; //We shouldn't handle move/resize events if shell is hidden.
	}

	if (!moved || oldX != x [0] || oldY != y [0]) {
		moved = true;
		oldX = x [0];
		oldY = y [0];
		sendEvent (SWT.Move);
		// widget could be disposed at this point
	}
	return 0;
}

@Override
long /*int*/ gtk_delete_event (long /*int*/ widget, long /*int*/ event) {
	if (isEnabled()) closeWidget ();
	return 1;
}

@Override
long /*int*/ gtk_enter_notify_event (long /*int*/ widget, long /*int*/ event) {
	if (widget != shellHandle) {
		return super.gtk_enter_notify_event (widget, event);
	}
	return 0;
}

@Override
long /*int*/ gtk_draw (long /*int*/ widget, long /*int*/ cairo) {
	if (widget == shellHandle) {
		if (isCustomResize ()) {
			int [] width = new int [1];
			int [] height = new int [1];
			long /*int*/ window = gtk_widget_get_window (widget);
			gdk_window_get_size (window, width, height);
			int border = GTK.gtk_container_get_border_width (widget);
			long /*int*/ context = GTK.gtk_widget_get_style_context (shellHandle);
			//TODO draw shell frame on GTK3
			GTK.gtk_style_context_save (context);
			GTK.gtk_render_frame (context, cairo, 0, 0, width [0], border);
			GTK.gtk_render_frame (context, cairo, 0, height [0] - border, width [0], border);
			GTK.gtk_render_frame (context, cairo, 0, border, border, height [0] - border - border);
			GTK.gtk_render_frame (context, cairo, width [0] - border, border, border, height [0] - border - border);
			GTK.gtk_render_frame (context, cairo, 0 + 10, 0 + 10, width [0] - 20, height [0] - 20);
			GTK.gtk_style_context_restore (context);
			return 1;
		}
		return 0;
	}
	return super.gtk_draw (widget, cairo);
}

@Override
long /*int*/ gtk_expose_event (long /*int*/ widget, long /*int*/ event) {
	if (widget == shellHandle) {
		if (isCustomResize ()) {
			GdkEventExpose gdkEventExpose = new GdkEventExpose ();
			OS.memmove (gdkEventExpose, event, GdkEventExpose.sizeof);
			long /*int*/ style = GTK.gtk_widget_get_style (widget);
			long /*int*/ window = gtk_widget_get_window (widget);
			int [] width = new int [1];
			int [] height = new int [1];
			gdk_window_get_size (window, width, height);
			GdkRectangle area = new GdkRectangle ();
			area.x = gdkEventExpose.area_x;
			area.y = gdkEventExpose.area_y;
			area.width = gdkEventExpose.area_width;
			area.height = gdkEventExpose.area_height;
			byte [] detail = Converter.wcsToMbcs ("base", true); //$NON-NLS-1$
			int border = GTK.gtk_container_get_border_width (widget);
			int state = display.activeShell == this ? GTK.GTK_STATE_SELECTED : GTK.GTK_STATE_PRELIGHT;
			GTK.gtk_paint_flat_box (style, window, state, GTK.GTK_SHADOW_NONE, area, widget, detail, 0, 0, width [0], border);
			GTK.gtk_paint_flat_box (style, window, state, GTK.GTK_SHADOW_NONE, area, widget, detail, 0, height [0] - border, width [0], border);
			GTK.gtk_paint_flat_box (style, window, state, GTK.GTK_SHADOW_NONE, area, widget, detail, 0, border, border, height [0] - border - border);
			GTK.gtk_paint_flat_box (style, window, state, GTK.GTK_SHADOW_NONE, area, widget, detail, width [0] - border, border, border, height [0] - border - border);
			GTK.gtk_paint_box (style, window, state, GTK.GTK_SHADOW_OUT, area, widget, detail, 0, 0, width [0], height [0]);
			return 1;
		}
		return 0;
	}
	return super.gtk_expose_event (widget, event);
}

@Override
long /*int*/ gtk_focus (long /*int*/ widget, long /*int*/ directionType) {
	switch ((int)/*64*/directionType) {
		case GTK.GTK_DIR_TAB_FORWARD:
		case GTK.GTK_DIR_TAB_BACKWARD:
			Control control = display.getFocusControl ();
			if (control != null) {
				if ((control.state & CANVAS) != 0 && (control.style & SWT.EMBEDDED) != 0 && control.getShell () == this) {
					int traversal = directionType == GTK.GTK_DIR_TAB_FORWARD ? SWT.TRAVERSE_TAB_NEXT : SWT.TRAVERSE_TAB_PREVIOUS;
					control.traverse (traversal);
					return 1;
				}
			}
			break;
	}
	return super.gtk_focus (widget, directionType);
}

@Override
long /*int*/ gtk_focus_in_event (long /*int*/ widget, long /*int*/ event) {
	if (widget != shellHandle) {
		return super.gtk_focus_in_event (widget, event);
	}
	display.activeShell = this;
	display.activePending = false;
	sendEvent (SWT.Activate);
	return 0;
}

@Override
long /*int*/ gtk_focus_out_event (long /*int*/ widget, long /*int*/ event) {
	if (widget != shellHandle) {
		return super.gtk_focus_out_event (widget, event);
	}
	Display display = this.display;
	sendEvent (SWT.Deactivate);
	setActiveControl (null);
	if (display.activeShell == this && !ignoreFocusOut) {
		display.activeShell = null;
		display.activePending = false;
	}
	return 0;
}

@Override
long /*int*/ gtk_leave_notify_event (long /*int*/ widget, long /*int*/ event) {
	if (widget == shellHandle) {
		if (isCustomResize ()) {
			GdkEventCrossing gdkEvent = new GdkEventCrossing ();
			OS.memmove (gdkEvent, event, GdkEventCrossing.sizeof);
			if ((gdkEvent.state & GDK.GDK_BUTTON1_MASK) == 0) {
				long /*int*/ window = gtk_widget_get_window (shellHandle);
				GDK.gdk_window_set_cursor (window, 0);
				display.resizeMode = 0;
			}
		}
		return 0;
	}
	return super.gtk_leave_notify_event (widget, event);
}

@Override
long /*int*/ gtk_move_focus (long /*int*/ widget, long /*int*/ directionType) {
	Control control = display.getFocusControl ();
	if (control != null) {
		long /*int*/ focusHandle = control.focusHandle ();
		GTK.gtk_widget_child_focus (focusHandle, (int)/*64*/directionType);
	}
	OS.g_signal_stop_emission_by_name (shellHandle, OS.move_focus);
	return 1;
}

@Override
long /*int*/ gtk_motion_notify_event (long /*int*/ widget, long /*int*/ event) {
	if (widget == shellHandle) {
		if (isCustomResize ()) {
			GdkEventMotion gdkEvent = new GdkEventMotion ();
			OS.memmove (gdkEvent, event, GdkEventMotion.sizeof);
			if ((gdkEvent.state & GDK.GDK_BUTTON1_MASK) != 0) {
				int border = GTK.gtk_container_get_border_width (shellHandle);
				int dx = (int)(gdkEvent.x_root - display.resizeLocationX);
				int dy = (int)(gdkEvent.y_root - display.resizeLocationY);
				int x = display.resizeBoundsX;
				int y = display.resizeBoundsY;
				int width = display.resizeBoundsWidth;
				int height = display.resizeBoundsHeight;
				int newWidth = Math.max(width - dx, Math.max(minWidth, border + border));
				int newHeight = Math.max(height - dy, Math.max(minHeight, border + border));
				switch (display.resizeMode) {
					case GDK.GDK_LEFT_SIDE:
						x += width - newWidth;
						width = newWidth;
						break;
					case GDK.GDK_TOP_LEFT_CORNER:
						x += width - newWidth;
						width = newWidth;
						y += height - newHeight;
						height = newHeight;
						break;
					case GDK.GDK_TOP_SIDE:
						y += height - newHeight;
						height = newHeight;
						break;
					case GDK.GDK_TOP_RIGHT_CORNER:
						width = Math.max(width + dx, Math.max(minWidth, border + border));
						y += height - newHeight;
						height = newHeight;
						break;
					case GDK.GDK_RIGHT_SIDE:
						width = Math.max(width + dx, Math.max(minWidth, border + border));
						break;
					case GDK.GDK_BOTTOM_RIGHT_CORNER:
						width = Math.max(width + dx, Math.max(minWidth, border + border));
						height = Math.max(height + dy, Math.max(minHeight, border + border));
						break;
					case GDK.GDK_BOTTOM_SIDE:
						height = Math.max(height + dy, Math.max(minHeight, border + border));
						break;
					case GDK.GDK_BOTTOM_LEFT_CORNER:
						x += width - newWidth;
						width = newWidth;
						height = Math.max(height + dy, Math.max(minHeight, border + border));
						break;
				}
				if (x != display.resizeBoundsX || y != display.resizeBoundsY) {
					GDK.gdk_window_move_resize (gtk_widget_get_window (shellHandle), x, y, width, height);
				} else {
					GTK.gtk_window_resize (shellHandle, width, height);
				}
			} else {
				int mode = getResizeMode (gdkEvent.x, gdkEvent.y);
				if (mode != display.resizeMode) {
					long /*int*/ window = gtk_widget_get_window (shellHandle);
					long /*int*/ cursor = GDK.gdk_cursor_new_for_display (GDK.gdk_display_get_default(), mode);
					GDK.gdk_window_set_cursor (window, cursor);
					gdk_cursor_unref (cursor);
					display.resizeMode = mode;
				}
			}
		}
		return 0;
	}
	return super.gtk_motion_notify_event (widget, event);
}

@Override
long /*int*/ gtk_key_press_event (long /*int*/ widget, long /*int*/ event) {
	if (widget == shellHandle) {
		/* Stop menu mnemonics when the shell is disabled */
		if ((state & DISABLED) != 0) return 1;

		if (menuBar != null && !menuBar.isDisposed ()) {
			Control focusControl = display.getFocusControl ();
			if (focusControl != null && (focusControl.hooks (SWT.KeyDown) || focusControl.filters (SWT.KeyDown))) {
				long /*int*/ [] accel = new long /*int*/ [1];
				long /*int*/ setting = GTK.gtk_settings_get_default ();
				OS.g_object_get (setting, GTK.gtk_menu_bar_accel, accel, 0);
				if (accel [0] != 0) {
					int [] keyval = new int [1];
					int [] mods = new int [1];
					GTK.gtk_accelerator_parse (accel [0], keyval, mods);
					OS.g_free (accel [0]);
					if (keyval [0] != 0) {
						GdkEventKey keyEvent = new GdkEventKey ();
						OS.memmove (keyEvent, event, GdkEventKey.sizeof);
						int mask = GTK.gtk_accelerator_get_default_mod_mask ();
						if (keyEvent.keyval == keyval [0] && (keyEvent.state & mask) == (mods [0] & mask)) {
							return focusControl.gtk_key_press_event (focusControl.focusHandle (), event);
						}
					}
				}
			}
		}
		return 0;
	}
	return super.gtk_key_press_event (widget, event);
}

@Override
long /*int*/ gtk_size_allocate (long /*int*/ widget, long /*int*/ allocation) {
	int width, height;
	if (GTK.GTK_VERSION >= OS.VERSION(3, 6, 0)) {
		int[] widthA = new int [1];
		int[] heightA = new int [1];
		GTK.gtk_window_get_size(shellHandle, widthA, heightA);
		width = widthA[0];
		height = heightA[0];
	} else {
		GtkAllocation widgetAllocation = new GtkAllocation ();
		GTK.gtk_widget_get_allocation (shellHandle, widgetAllocation);
		width = widgetAllocation.width;
		height = widgetAllocation.height;
	}

	//	Bug 474235: on Wayland gtk_size_allocate() is called more frequently, causing an
	//  infinitely recursive resize call. This causes non-resizable Shells/Dialogs to
	//  crash. Fix: only call resizeBounds() on resizable Shells.
	if ((!resized || oldWidth != width || oldHeight != height)
			&& (GTK.GTK3 && !OS.isX11() ? ((style & SWT.RESIZE) != 0) : true)) {  //Wayland
		oldWidth = width;
		oldHeight = height;
		resizeBounds (width, height, true); //this is called to resize child widgets when the shell is resized.
	}
	return 0;
}

@Override
long /*int*/ gtk_realize (long /*int*/ widget) {
	long /*int*/ result = super.gtk_realize (widget);
	long /*int*/ window = gtk_widget_get_window (shellHandle);
	if ((style & SWT.SHELL_TRIM) != SWT.SHELL_TRIM) {
		int decorations = 0;
		int functions = 0;
		if ((style & SWT.NO_TRIM) == 0) {
				if ((style & SWT.MIN) != 0) {
					decorations |= GDK.GDK_DECOR_MINIMIZE;
					functions |= GDK.GDK_FUNC_MINIMIZE;
				}
				if ((style & SWT.MAX) != 0) {
					decorations |= GDK.GDK_DECOR_MAXIMIZE;
					functions |= GDK.GDK_FUNC_MAXIMIZE;
				}
				if ((style & SWT.RESIZE) != 0) {
					decorations |= GDK.GDK_DECOR_RESIZEH;
					functions |= GDK.GDK_FUNC_RESIZE;
				}
			if ((style & SWT.BORDER) != 0) decorations |= GDK.GDK_DECOR_BORDER;
			if ((style & SWT.MENU) != 0) decorations |= GDK.GDK_DECOR_MENU;
			if ((style & SWT.TITLE) != 0) decorations |= GDK.GDK_DECOR_TITLE;
			if ((style & SWT.CLOSE) != 0) functions |= GDK.GDK_FUNC_CLOSE;
			/*
			* Feature in GTK.  Under some Window Managers (Sawmill), in order
			* to get any border at all from the window manager it is necessary to
			* set GDK_DECOR_BORDER.  The fix is to force these bits when any
			* kind of border is requested.
			*/
			if ((style & SWT.RESIZE) != 0) decorations |= GDK.GDK_DECOR_BORDER;
			if ((style & SWT.NO_MOVE) == 0) functions |=  GDK.GDK_FUNC_MOVE;
		}
		GDK.gdk_window_set_decorations (window, decorations);

		/*
		* For systems running Metacity, this call forces the style hints to
		* be displayed in a window's titlebar. Otherwise, the decorations
		* set by the function gdk_window_set_decorations (window,
		* decorations) are ignored by the window manager.
		*/
		GDK.gdk_window_set_functions(window, functions);
	} else if ((style & SWT.NO_MOVE) != 0) {
		// if the GDK_FUNC_ALL bit is present, all the other style
		// bits specified as a parameter will be removed from the window
		GDK.gdk_window_set_functions (window, GDK.GDK_FUNC_ALL | GDK.GDK_FUNC_MOVE);
	}
	if ((style & SWT.ON_TOP) != 0) {
		GDK.gdk_window_set_override_redirect (window, true);
	}
	return result;
}

@Override
long /*int*/ gtk_window_state_event (long /*int*/ widget, long /*int*/ event) {
	GdkEventWindowState gdkEvent = new GdkEventWindowState ();
	OS.memmove (gdkEvent, event, GdkEventWindowState.sizeof);
	minimized = (gdkEvent.new_window_state & GDK.GDK_WINDOW_STATE_ICONIFIED) != 0;
	maximized = (gdkEvent.new_window_state & GDK.GDK_WINDOW_STATE_MAXIMIZED) != 0;
	fullScreen = (gdkEvent.new_window_state & GDK.GDK_WINDOW_STATE_FULLSCREEN) != 0;
	if ((gdkEvent.changed_mask & GDK.GDK_WINDOW_STATE_ICONIFIED) != 0) {
		if (minimized) {
			sendEvent (SWT.Iconify);
		} else {
			sendEvent (SWT.Deiconify);
		}
		updateMinimized (minimized);
	}
	return 0;
}

/**
 * Moves the receiver to the top of the drawing order for
 * the display on which it was created (so that all other
 * shells on that display, which are not the receiver's
 * children will be drawn behind it), marks it visible,
 * sets the focus and asks the window manager to make the
 * shell active.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see Control#moveAbove
 * @see Control#setFocus
 * @see Control#setVisible
 * @see Display#getActiveShell
 * @see Decorations#setDefaultButton(Button)
 * @see Shell#setActive
 * @see Shell#forceActive
 */
public void open () {
	checkWidget ();
	bringToTop (false);
	if (Shell.class.isInstance(getParent()) && !getParent().isVisible()) {
		Shell.class.cast(getParent()).open();
	}
	setVisible (true);
	if (isDisposed ()) return;
	/*
	 * When no widget has been given focus, or another push button has focus,
	 * give focus to the default button. This avoids overriding the default
	 * button.
	 */
	boolean restored = restoreFocus ();
	if (!restored) {
		restored = traverseGroup (true);
	}
	if (restored) {
		Control focusControl = display.getFocusControl ();
		if (focusControl instanceof Button && (focusControl.style & SWT.PUSH) != 0) {
			restored = false;
		}
	}
	if (!restored) {
		/* If a shell is opened during the FocusOut event of a widget,
		 * it is required to set focus to all shells except for ON_TOP
		 * shells in order to maintain consistency with other platforms.
		 */
		if ((style & SWT.ON_TOP) == 0) display.focusEvent = SWT.None;

		if (defaultButton != null && !defaultButton.isDisposed ()) {
			defaultButton.setFocus ();
		} else {
			setFocus ();
		}
	}
}

@Override
public boolean print (GC gc) {
	checkWidget ();
	if (gc == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (gc.isDisposed ()) error (SWT.ERROR_INVALID_ARGUMENT);
	return false;
}

/**
 * Removes the listener from the collection of listeners who will
 * be notified when operations are performed on the receiver.
 *
 * @param listener the listener which should no longer be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see ShellListener
 * @see #addShellListener
 */
public void removeShellListener (ShellListener listener) {
	checkWidget();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (SWT.Close, listener);
	eventTable.unhook (SWT.Iconify,listener);
	eventTable.unhook (SWT.Deiconify,listener);
	eventTable.unhook (SWT.Activate, listener);
	eventTable.unhook (SWT.Deactivate, listener);
}

void removeTooTip (ToolTip toolTip) {
	if (toolTips == null) return;
	for (int i=0; i<toolTips.length; i++) {
		if (toolTips [i] == toolTip) {
			toolTips [i] = null;
			return;
		}
	}
}

@Override
void reskinChildren (int flags) {
	Shell [] shells = getShells ();
	for (int i=0; i<shells.length; i++) {
		Shell shell = shells [i];
		if (shell != null) shell.reskin (flags);
	}
	if (toolTips != null) {
		for (int i=0; i<toolTips.length; i++) {
			ToolTip toolTip = toolTips [i];
			if (toolTip != null) toolTip.reskin (flags);
		}
	}
	super.reskinChildren (flags);
}

/**
 * If the receiver is visible, moves it to the top of the
 * drawing order for the display on which it was created
 * (so that all other shells on that display, which are not
 * the receiver's children will be drawn behind it) and asks
 * the window manager to make the shell active
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @since 2.0
 * @see Control#moveAbove
 * @see Control#setFocus
 * @see Control#setVisible
 * @see Display#getActiveShell
 * @see Decorations#setDefaultButton(Button)
 * @see Shell#open
 * @see Shell#setActive
 */
public void setActive () {
	checkWidget ();
	bringToTop (false);
}

void setActiveControl (Control control) {
	setActiveControl (control, SWT.None);
}

void setActiveControl (Control control, int type) {
	if (control != null && control.isDisposed ()) control = null;
	if (lastActive != null && lastActive.isDisposed ()) lastActive = null;
	if (lastActive == control) return;

	/*
	* Compute the list of controls to be activated and
	* deactivated by finding the first common parent
	* control.
	*/
	Control [] activate = (control == null) ? new Control[0] : control.getPath ();
	Control [] deactivate = (lastActive == null) ? new Control[0] : lastActive.getPath ();
	lastActive = control;
	int index = 0, length = Math.min (activate.length, deactivate.length);
	while (index < length) {
		if (activate [index] != deactivate [index]) break;
		index++;
	}

	/*
	* It is possible (but unlikely), that application
	* code could have destroyed some of the widgets. If
	* this happens, keep processing those widgets that
	* are not disposed.
	*/
	for (int i=deactivate.length-1; i>=index; --i) {
		if (!deactivate [i].isDisposed ()) {
			deactivate [i].sendEvent (SWT.Deactivate);
		}
	}
	for (int i=activate.length-1; i>=index; --i) {
		if (!activate [i].isDisposed ()) {
			Event event = new Event ();
			event.detail = type;
			activate [i].sendEvent (SWT.Activate, event);
		}
	}
}

/**
 * Sets the receiver's alpha value which must be
 * between 0 (transparent) and 255 (opaque).
 * <p>
 * This operation requires the operating system's advanced
 * widgets subsystem which may not be available on some
 * platforms.
 * </p>
 * @param alpha the alpha value
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @since 3.4
 */
public void setAlpha (int alpha) {
	checkWidget ();
	if (GTK.gtk_widget_is_composited (shellHandle)) {
		/*
		 * Feature in GTK: gtk_window_set_opacity() is deprecated on GTK3.8
		 * onward. Use gtk_widget_set_opacity() instead.
		 */
		if (GTK.GTK_VERSION > OS.VERSION (3, 8, 0)) {
			GTK.gtk_widget_set_opacity (shellHandle, (double) alpha / 255);
		} else {
			alpha &= 0xFF;
			GTK.gtk_window_set_opacity (shellHandle, alpha / 255f);
		}
	}
}

void resizeBounds (int width, int height, boolean notify) {
	if (redrawWindow != 0) {
		GDK.gdk_window_resize (redrawWindow, width, height);
	}
	if (enableWindow != 0) {
		GDK.gdk_window_resize (enableWindow, width, height);
	}
	int border = GTK.gtk_container_get_border_width (shellHandle);
	int boxWidth = width - 2*border;
	int boxHeight = height - 2*border;
	if (!GTK.GTK3 || (style & SWT.RESIZE) == 0) {
		GTK.gtk_widget_set_size_request (vboxHandle, boxWidth, boxHeight);
	}
	forceResize (boxWidth, boxHeight);
	if (notify) {
		resized = true;
		sendEvent (SWT.Resize);
		if (isDisposed ()) return;
		if (layout != null) {
			markLayout (false, false);
			updateLayout (false);
		}
	}
}

@Override
int setBounds (int x, int y, int width, int height, boolean move, boolean resize) {
	// bug in GTK2 crashes JVM, in GTK3 the new shell only. See bug 472743
	width = Math.min(width, (2 << 14) - 1);
	height = Math.min(height, (2 << 14) - 1);

	if (fullScreen) setFullScreen (false);
	/*
	* Bug in GTK.  When either of the location or size of
	* a shell is changed while the shell is maximized, the
	* shell is moved to (0, 0).  The fix is to explicitly
	* unmaximize the shell before setting the bounds to
	* anything different from the current bounds.
	*/
	if (getMaximized ()) {
		Rectangle rect = getBoundsInPixels ();
		boolean sameOrigin = !move || (rect.x == x && rect.y == y);
		boolean sameExtent = !resize || (rect.width == width && rect.height == height);
		if (sameOrigin && sameExtent) return 0;
		setMaximized (false);
	}
	int result = 0;
	if (move) {
		int [] x_pos = new int [1], y_pos = new int [1];
		GTK.gtk_window_get_position (shellHandle, x_pos, y_pos);
		GTK.gtk_window_move (shellHandle, x, y);
		/*
		 * Bug in GTK: gtk_window_get_position () is not always up-to-date right after
		 * gtk_window_move (). The random delays cause problems like bug 445900.
		 *
		 * The workaround is to wait for the position change to be processed.
		 * The limit 1000 is an experimental value. I've seen cases where about 200
		 * iterations were necessary.
		 */
		for (int i = 0; i < 1000; i++) {
			int [] x2_pos = new int [1], y2_pos = new int [1];
			GTK.gtk_window_get_position (shellHandle, x2_pos, y2_pos);
			if (x2_pos[0] == x && y2_pos[0] == y) {
				break;
			}
		}
		if (x_pos [0] != x || y_pos [0] != y) {
			moved = true;
			oldX = x;
			oldY = y;
			sendEvent (SWT.Move);
			if (isDisposed ()) return 0;
			result |= MOVED;
		}
	}
	if (resize) {
		width = Math.max (1, Math.max (minWidth, width - trimWidth ()));
		height = Math.max (1, Math.max (minHeight, height - trimHeight ()));
		/*
		* If the shell is created without a RESIZE style bit, and the
		* minWidth/minHeight has been set, allow the resize.
		*/
		if ((style & SWT.RESIZE) != 0 || (minHeight != 0 || minWidth != 0)) GTK.gtk_window_resize (shellHandle, width, height);
		boolean changed = width != oldWidth || height != oldHeight;
		if (changed) {
			oldWidth = width;
			oldHeight = height;
			result |= RESIZED;
		}
		resizeBounds (width, height, changed);
	}
	return result;
}

@Override
void setCursor (long /*int*/ cursor) {
	if (enableWindow != 0) {
		GDK.gdk_window_set_cursor (enableWindow, cursor);
		GDK.gdk_flush ();
	}
	super.setCursor (cursor);
}

@Override
public void setEnabled (boolean enabled) {
	checkWidget();
	if (((state & DISABLED) == 0) == enabled) return;
	Display display = this.display;
	Control control = null;
	boolean fixFocus = false;
	if (!enabled) {
		if (display.focusEvent != SWT.FocusOut) {
			control = display.getFocusControl ();
			fixFocus = isFocusAncestor (control);
		}
	}
	if (enabled) {
		state &= ~DISABLED;
	} else {
		state |= DISABLED;
	}
	enableWidget (enabled);
	if (isDisposed ()) return;
	if (enabled) {
		if (enableWindow != 0) {
			cleanupEnableWindow();
		}
	} else {
		long /*int*/ parentHandle = shellHandle;
		GTK.gtk_widget_realize (parentHandle);
		long /*int*/ window = gtk_widget_get_window (parentHandle);
		Rectangle rect = getBoundsInPixels ();
		GdkWindowAttr attributes = new GdkWindowAttr ();
		attributes.width = rect.width;
		attributes.height = rect.height;
		attributes.event_mask = (0xFFFFFFFF & ~OS.ExposureMask);
		attributes.wclass = GDK.GDK_INPUT_ONLY;
		attributes.window_type = GDK.GDK_WINDOW_CHILD;
		enableWindow = GDK.gdk_window_new (window, attributes, 0);
		if (enableWindow != 0) {
			if (cursor != null) {
				GDK.gdk_window_set_cursor (enableWindow, cursor.handle);
				GDK.gdk_flush ();
			}
			/* 427776: we need to listen to all enter-notify-event signals to
			 * see if this new GdkWindow has been added to a widget's internal
			 * hash table, so when the GdkWindow is destroyed we can also remove
			 * that reference. */
			if (enterNotifyEventFunc != null)
				enterNotifyEventId = OS.g_signal_add_emission_hook (enterNotifyEventSignalId, 0, enterNotifyEventFunc.getAddress (), enableWindow, 0);

			GDK.gdk_window_set_user_data (enableWindow, parentHandle);
			GDK.gdk_window_show (enableWindow);
		}
	}
	if (fixFocus) fixFocus (control);
	if (enabled && display.activeShell == this) {
		if (!restoreFocus ()) traverseGroup (false);
	}
}

/**
 * Sets the full screen state of the receiver.
 * If the argument is <code>true</code> causes the receiver
 * to switch to the full screen state, and if the argument is
 * <code>false</code> and the receiver was previously switched
 * into full screen state, causes the receiver to switch back
 * to either the maximized or normal states.
 * <p>
 * Note: The result of intermixing calls to <code>setFullScreen(true)</code>,
 * <code>setMaximized(true)</code> and <code>setMinimized(true)</code> will
 * vary by platform. Typically, the behavior will match the platform user's
 * expectations, but not always. This should be avoided if possible.
 * </p>
 *
 * @param fullScreen the new fullscreen state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @since 3.4
 */
public void setFullScreen (boolean fullScreen) {
	checkWidget();
	if (fullScreen) {
		GTK.gtk_window_fullscreen (shellHandle);
	} else {
		GTK.gtk_window_unfullscreen (shellHandle);
		if (maximized) {
			setMaximized (true);
		}
	}
	this.fullScreen = fullScreen;
}

/**
 * Sets the input method editor mode to the argument which
 * should be the result of bitwise OR'ing together one or more
 * of the following constants defined in class <code>SWT</code>:
 * <code>NONE</code>, <code>ROMAN</code>, <code>DBCS</code>,
 * <code>PHONETIC</code>, <code>NATIVE</code>, <code>ALPHA</code>.
 *
 * @param mode the new IME mode
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see SWT
 */
public void setImeInputMode (int mode) {
	checkWidget();
}

@Override
void setInitialBounds () {
	int width, height;
	if ((state & FOREIGN_HANDLE) != 0) {
		GtkAllocation allocation = new GtkAllocation ();
		GTK.gtk_widget_get_allocation (shellHandle, allocation);
		width = allocation.width;
		height = allocation.height;
	} else {
		width = GDK.gdk_screen_width () * 5 / 8;
		height = GDK.gdk_screen_height () * 5 / 8;
		long /*int*/ screen = GDK.gdk_screen_get_default ();
		if (screen != 0) {
			if (GDK.gdk_screen_get_n_monitors (screen) > 1) {
				int monitorNumber = GDK.gdk_screen_get_monitor_at_window (screen, paintWindow ());
				GdkRectangle dest = new GdkRectangle ();
				GDK.gdk_screen_get_monitor_geometry (screen, monitorNumber, dest);
				width = dest.width * 5 / 8;
				height = dest.height * 5 / 8;
			}
		}
		if ((style & SWT.RESIZE) != 0) {
			GTK.gtk_window_resize (shellHandle, width, height);
		}
	}
	resizeBounds (width, height, false);
}

@Override
public void setMaximized (boolean maximized) {
	checkWidget();
	super.setMaximized (maximized);
	if (maximized) {
		GTK.gtk_window_maximize (shellHandle);
	} else {
		GTK.gtk_window_unmaximize (shellHandle);
	}
}

@Override
public void setMenuBar (Menu menu) {
	checkWidget();
	if (menuBar == menu) return;
	boolean both = menu != null && menuBar != null;
	if (menu != null) {
		if ((menu.style & SWT.BAR) == 0) error (SWT.ERROR_MENU_NOT_BAR);
		if (menu.parent != this) error (SWT.ERROR_INVALID_PARENT);
	}
	if (menuBar != null) {
		long /*int*/ menuHandle = menuBar.handle;
		GTK.gtk_widget_hide (menuHandle);
		destroyAccelGroup ();
	}
	menuBar = menu;
	if (menuBar != null) {
		long /*int*/ menuHandle = menu.handle;
		GTK.gtk_widget_show (menuHandle);
		createAccelGroup ();
		menuBar.addAccelerators (accelGroup);
	}
	GtkAllocation allocation = new GtkAllocation ();
	GTK.gtk_widget_get_allocation (vboxHandle, allocation);
	int width = allocation.width;
	int height = allocation.height;
	resizeBounds (width, height, !both);
}

@Override
public void setMinimized (boolean minimized) {
	checkWidget();
	if (this.minimized == minimized) return;
	super.setMinimized (minimized);
	if(GTK.GTK_VERSION >= OS.VERSION (3, 8, 0) && !GTK.gtk_widget_get_visible(shellHandle)) {
		GTK.gtk_widget_show(shellHandle);
	}
	if (minimized) {
		GTK.gtk_window_iconify (shellHandle);
	} else {
		GTK.gtk_window_deiconify (shellHandle);
		bringToTop (false);
	}
}

/**
 * Sets the receiver's minimum size to the size specified by the arguments.
 * If the new minimum size is larger than the current size of the receiver,
 * the receiver is resized to the new minimum size.
 *
 * @param width the new minimum width for the receiver
 * @param height the new minimum height for the receiver
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @since 3.1
 */
public void setMinimumSize (int width, int height) {
	checkWidget ();
	setMinimumSize (new Point (width, height));
}

void setMinimumSizeInPixels (int width, int height) {
	checkWidget ();
	GdkGeometry geometry = new GdkGeometry ();
	minWidth = geometry.min_width = Math.max (width, trimWidth ()) - trimWidth ();
	minHeight = geometry.min_height = Math.max (height, trimHeight ()) - trimHeight ();
	GTK.gtk_window_set_geometry_hints (shellHandle, 0, geometry, GDK.GDK_HINT_MIN_SIZE);
}

/**
 * Sets the receiver's minimum size to the size specified by the argument.
 * If the new minimum size is larger than the current size of the receiver,
 * the receiver is resized to the new minimum size.
 *
 * @param size the new minimum size for the receiver
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @since 3.1
 */
public void setMinimumSize (Point size) {
	checkWidget ();
	setMinimumSizeInPixels (DPIUtil.autoScaleUp (size));
}

void setMinimumSizeInPixels (Point size) {
	checkWidget ();
	if (size == null) error (SWT.ERROR_NULL_ARGUMENT);
	setMinimumSizeInPixels (size.x, size.y);
}

/**
 * Sets the receiver's modified state as specified by the argument.
 *
 * @param modified the new modified state for the receiver
 *
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @since 3.5
 */
public void setModified (boolean modified) {
	checkWidget ();
	this.modified = modified;
}

/**
 * Sets the shape of the shell to the region specified
 * by the argument.  When the argument is null, the
 * default shape of the shell is restored.  The shell
 * must be created with the style SWT.NO_TRIM in order
 * to specify a region.
 * <p>
 * NOTE: This method also sets the size of the shell. Clients should
 * not call {@link #setSize} or {@link #setBounds} on this shell.
 * Furthermore, the passed region should not be modified any more.
 * </p>
 *
 * @param region the region that defines the shape of the shell (or null)
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if the region has been disposed</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @since 3.0
 */
@Override
public void setRegion (Region region) {
	checkWidget ();
	if ((style & SWT.NO_TRIM) == 0) return;

	if (region != null) {
		Rectangle bounds = region.getBounds ();
		setSize (bounds.x + bounds.width, bounds.y + bounds.height);
	}
	Region regionToDispose = null;
	if ((style & SWT.RIGHT_TO_LEFT) != 0) {
		if (originalRegion != null) regionToDispose = this.region;
		originalRegion = region;
		region = mirrorRegion (region);
	} else {
		originalRegion = null;
	}
	super.setRegion (region);
	if (regionToDispose != null) regionToDispose.dispose();
}

//copied from Region:
static void gdk_region_get_rectangles(long /*int*/ region, long /*int*/[] rectangles, int[] n_rectangles) {
	if (!GTK.GTK3) {
		GDK.gdk_region_get_rectangles (region, rectangles, n_rectangles);
		return;
	}
	int num = Cairo.cairo_region_num_rectangles (region);
	if (n_rectangles != null) n_rectangles[0] = num;
	rectangles[0] = OS.g_malloc(GdkRectangle.sizeof * num);
	for (int n = 0; n < num; n++) {
		Cairo.cairo_region_get_rectangle (region, n, rectangles[0] + (n * GdkRectangle.sizeof));
	}
}

static Region mirrorRegion (Region region) {
	if (region == null) return null;

	Region mirrored = new Region (region.getDevice ());

	long /*int*/ rgn = region.handle;
	int [] nRects = new int [1];
	long /*int*/ [] rects = new long /*int*/ [1];
	gdk_region_get_rectangles (rgn, rects, nRects);
	Rectangle bounds = DPIUtil.autoScaleUp(region.getBounds ());
	GdkRectangle rect = new GdkRectangle ();
	for (int i = 0; i < nRects [0]; i++) {
		OS.memmove (rect, rects[0] + (i * GdkRectangle.sizeof), GdkRectangle.sizeof);
		rect.x = bounds.x + bounds.width - rect.x - rect.width;
		GDK.gdk_region_union_with_rect (mirrored.handle, rect);
	}
	if (rects [0] != 0) OS.g_free (rects [0]);
	return mirrored;
}

/*
 * Shells are never labelled by other widgets, so no initialization is needed.
 */
@Override
void setRelations() {
}

@Override
public void setText (String string) {
	super.setText (string);

	/*
	* GTK bug 82013.  For some reason, if the title string
	* is less than 7 bytes long and is not terminated by
	* a space, some window managers occasionally draw
	* garbage after the last character in  the title.
	* The fix is to pad the title.
	*/
	int length = string.length ();
	char [] chars = new char [Math.max (6, length) + 1];
	string.getChars (0, length , chars, 0);
	for (int i=length; i<chars.length; i++)  chars [i] = ' ';
	byte [] buffer = Converter.wcsToMbcs (chars, true);
	GTK.gtk_window_set_title (shellHandle, buffer);
}

@Override
public void setVisible (boolean visible) {
	checkWidget();

	if (moved) { //fix shell location if it was moved.
		setLocationInPixels(oldX, oldY);
	}
	int mask = SWT.PRIMARY_MODAL | SWT.APPLICATION_MODAL | SWT.SYSTEM_MODAL;
	if ((style & mask) != 0) {
		if (visible) {
			display.setModalShell (this);
			GTK.gtk_window_set_modal (shellHandle, true);
		} else {
			display.clearModal (this);
			GTK.gtk_window_set_modal (shellHandle, false);
		}
		/*
		 * When in full-screen mode, the OS will always consider it to be the top of the display stack unless it is a dialog.
		 * This fix will give modal windows dialog-type priority if the parent is in full-screen, allowing it to be popped
		 * up in front of the full-screen window.
		 */
		if (parent!=null && parent.getShell().getFullScreen()) {
			GTK.gtk_window_set_type_hint(shellHandle, GDK.GDK_WINDOW_TYPE_HINT_DIALOG);
		}
	} else {
		updateModal ();
	}
	showWithParent = visible;
	if (GTK.gtk_widget_get_mapped (shellHandle) == visible) return;
	if (visible) {
		if (center && !moved) {
			center ();
			if (isDisposed ()) return;
		}
		sendEvent (SWT.Show);
		if (isDisposed ()) return;

		/*
		* In order to ensure that the shell is visible
		* and fully painted, dispatch events such as
		* GDK_MAP and GDK_CONFIGURE, until the GDK_MAP
		* event for the shell is received.
		*
		* Note that if the parent is minimized or withdrawn
		* from the desktop, this should not be done since
		* the shell not will be mapped until the parent is
		* unminimized or shown on the desktop.
		*/
		mapped = false;
		/**
		 *  Feature in GTK: This handles grabbing the keyboard focus from a SWT.ON_TOP window
		 *  if it has editable fields and is running Wayland. Refer to bug 515773.
		 */
		GTK.gtk_widget_show (shellHandle);
		if (enableWindow != 0) GDK.gdk_window_raise (enableWindow);
		if (isDisposed ()) return;
		if (!(OS.isX11() && GTK.GTK_IS_PLUG (shellHandle))) {
			display.dispatchEvents = new int [] {
				GDK.GDK_EXPOSE,
				GDK.GDK_FOCUS_CHANGE,
				GDK.GDK_CONFIGURE,
				GDK.GDK_MAP,
				GDK.GDK_UNMAP,
				GDK.GDK_NO_EXPOSE,
				GDK.GDK_WINDOW_STATE
			};
			Display display = this.display;
			display.putGdkEvents();
			boolean iconic = false;
			Shell shell = parent != null ? parent.getShell() : null;
			do {
				/*
				* This call to gdk_threads_leave() is a temporary work around
				* to avoid deadlocks when gdk_threads_init() is called by native
				* code outside of SWT (i.e AWT, etc). It ensures that the current
				* thread leaves the GTK lock before calling the function below.
				*/
				GDK.gdk_threads_leave();
				OS.g_main_context_iteration (0, false);
				if (isDisposed ()) break;
				iconic = minimized || (shell != null && shell.minimized);
			} while (!mapped && !iconic);
			display.dispatchEvents = null;
			if (isDisposed ()) return;
			if (!iconic) {
				update (true, true);
				if (isDisposed ()) return;
				adjustTrim ();
			}
		}
		mapped = true;

		if ((style & mask) != 0) {
			gdk_pointer_ungrab (GTK.gtk_widget_get_window (shellHandle), GDK.GDK_CURRENT_TIME);
		}
		opened = true;
		if (!moved) {
			moved = true;
			Point location = getLocationInPixels();
			oldX = location.x;
			oldY = location.y;
			sendEvent (SWT.Move);
			if (isDisposed ()) return;
		}
		if (!resized) {
			resized = true;
			Point size = getSizeInPixels ();
			oldWidth = size.x - trimWidth ();
			oldHeight = size.y - trimHeight ();
			sendEvent (SWT.Resize);
			if (isDisposed ()) return;
			if (layout != null) {
				markLayout (false, false);
				updateLayout (false);
			}
		}
	} else {
		fixActiveShell ();
		// Feature in Wayland: If the shell item is ON_TOP, remove its grab before hiding it, otherwise focus is locked to
		// the hidden widget and can never be returned.
		if (!OS.isX11() && GTK.GTK_VERSION >= OS.VERSION(3, 20, 0)) {
			if ((style & SWT.ON_TOP) != 0 && (style & SWT.NO_FOCUS) == 0) {
				GTK.gtk_grab_remove(shellHandle);
				GDK.gdk_seat_ungrab(GDK.gdk_display_get_default_seat(
							GDK.gdk_window_get_display(GTK.gtk_widget_get_window(shellHandle))));
			}
		}
		GTK.gtk_widget_hide (shellHandle);
		sendEvent (SWT.Hide);
	}
}

@Override
void setZOrder (Control sibling, boolean above, boolean fixRelations) {
	/*
	* Bug in GTK+.  Changing the toplevel window Z-order causes
	* X to send a resize event.  Before the shell is mapped, these
	* resize events always have a size of 200x200, causing extra
	* layout work to occur.  The fix is to modify the Z-order only
	* if the shell has already been mapped at least once.
	*/
	/* Shells are never included in labelled-by relations */
	if (mapped) setZOrder (sibling, above, false, false);
}

@Override
long /*int*/ shellMapProc (long /*int*/ handle, long /*int*/ arg0, long /*int*/ user_data) {
	mapped = true;
	display.dispatchEvents = null;
	return 0;
}

@Override
void showWidget () {
	if ((state & FOREIGN_HANDLE) != 0) {
		/*
		* In case of foreign handles, activeShell might not be initialised as
		* no focusIn events are generated on the window until the window loses
		* and gain focus.
		*/
		if (GTK.gtk_window_is_active (shellHandle)) {
			display.activeShell = this;
			display.activePending = true;
		}
		long /*int*/ children = GTK.gtk_container_get_children (shellHandle), list = children;
		while (list != 0) {
			GTK.gtk_container_remove (shellHandle, OS.g_list_data (list));
			list = OS.g_list_next(list);
		}
		OS.g_list_free (list);
	}
	GTK.gtk_container_add (shellHandle, vboxHandle);
	if (scrolledHandle != 0) GTK.gtk_widget_show (scrolledHandle);
	if (handle != 0) GTK.gtk_widget_show (handle);
	if (vboxHandle != 0) GTK.gtk_widget_show (vboxHandle);
}

@Override
long /*int*/ sizeAllocateProc (long /*int*/ handle, long /*int*/ arg0, long /*int*/ user_data) {
	int offset = 16;
	int [] x = new int [1], y = new int [1];
	gdk_window_get_device_position (0, x, y, null);
	y [0] += offset;
	long /*int*/ screen = GDK.gdk_screen_get_default ();
	if (screen != 0) {
		int monitorNumber = GDK.gdk_screen_get_monitor_at_point (screen, x[0], y[0]);
		GdkRectangle dest = new GdkRectangle ();
		GDK.gdk_screen_get_monitor_geometry (screen, monitorNumber, dest);
		GtkAllocation allocation = new GtkAllocation ();
		GTK.gtk_widget_get_allocation (handle, allocation);
		int width = allocation.width;
		int height = allocation.height;
		if (x[0] + width > dest.x + dest.width) {
			x [0] = (dest.x + dest.width) - width;
		}
		if (y[0] + height > dest.y + dest.height) {
			y[0] = (dest.y + dest.height) - height;
		}
	}
	GTK.gtk_window_move (handle, x [0], y [0]);
	return 0;
}

@Override
long /*int*/ sizeRequestProc (long /*int*/ handle, long /*int*/ arg0, long /*int*/ user_data) {
	GTK.gtk_widget_hide (handle);
	return 0;
}

@Override
boolean traverseEscape () {
	if (parent == null) return false;
	if (!isVisible () || !isEnabled ()) return false;
	close ();
	return true;
}
int trimHeight () {
	if ((style & SWT.NO_TRIM) != 0) return 0;
	if (fullScreen) return 0;
	// Shells with both ON_TOP and RESIZE set only use border, not trim.
	// See bug 319612.
	if (isCustomResize()) return 0;
	boolean hasTitle = false, hasResize = false, hasBorder = false;
	hasTitle = (style & (SWT.MIN | SWT.MAX | SWT.TITLE | SWT.MENU)) != 0;
	hasResize = (style & SWT.RESIZE) != 0;
	hasBorder = (style & SWT.BORDER) != 0;
	if (hasTitle) {
		if (hasResize) return display.trimHeights [Display.TRIM_TITLE_RESIZE];
		if (hasBorder) return display.trimHeights [Display.TRIM_TITLE_BORDER];
		return display.trimHeights [Display.TRIM_TITLE];
	}
	if (hasResize) return display.trimHeights [Display.TRIM_RESIZE];
	if (hasBorder) return display.trimHeights [Display.TRIM_BORDER];
	return display.trimHeights [Display.TRIM_NONE];
}

int trimWidth () {
	if ((style & SWT.NO_TRIM) != 0) return 0;
	if (fullScreen) return 0;
	// Shells with both ON_TOP and RESIZE set only use border, not trim.
	// See bug 319612.
	if (isCustomResize()) return 0;
	boolean hasTitle = false, hasResize = false, hasBorder = false;
	hasTitle = (style & (SWT.MIN | SWT.MAX | SWT.TITLE | SWT.MENU)) != 0;
	hasResize = (style & SWT.RESIZE) != 0;
	hasBorder = (style & SWT.BORDER) != 0;
	if (hasTitle) {
		if (hasResize) return display.trimWidths [Display.TRIM_TITLE_RESIZE];
		if (hasBorder) return display.trimWidths [Display.TRIM_TITLE_BORDER];
		return display.trimWidths [Display.TRIM_TITLE];
	}
	if (hasResize) return display.trimWidths [Display.TRIM_RESIZE];
	if (hasBorder) return display.trimWidths [Display.TRIM_BORDER];
	return display.trimWidths [Display.TRIM_NONE];
}

void updateModal () {
	if (OS.isX11() && GTK.GTK_IS_PLUG (shellHandle)) return;
	long /*int*/ group = 0;
	boolean isModalShell = false;
	if (display.getModalDialog () == null) {
		Shell modal = getModalShell ();
		int mask = SWT.PRIMARY_MODAL | SWT.APPLICATION_MODAL | SWT.SYSTEM_MODAL;
		Composite shell = null;
		if (modal == null) {
			if ((style & mask) != 0) {
				shell = this;
				/*
				* Feature in GTK. If a modal shell is reassigned to
				* a different group, then it's modal state is not.
				* persisted against the new group.
				* The fix is to reset the modality before it is changed
				* into a different group and then, set back after it
				* assigned into new group.
				*/
				isModalShell = GTK.gtk_window_get_modal (shellHandle);
				if (isModalShell) GTK.gtk_window_set_modal (shellHandle, false);
			}
		} else {
			shell = modal;
		}
		Composite topModalShell = shell;
		while (shell != null) {
			if ((shell.style & mask) == 0) {
				group = shell.getShell ().group;
				break;
			}
			topModalShell = shell;
			shell = shell.parent;
		}
		/*
		* If a modal shell doesn't have any parent (or modal shell as it's parent),
		* then we incorrectly add the modal shell to the default group, due to which
		* children of that modal shell are not interactive. The fix is to ensure
		* that whenever there is a modal shell in the hierarchy, then we always
		* add the modal shell's group to that modal shell and it's modelless children
		* in a different group.
		*/
		if (group == 0 && topModalShell != null) group = topModalShell.getShell ().group;
	}
	if (group == 0) {
		/*
		* Feature in GTK. Starting with GTK version 2.10, GTK
		* doesn't assign windows to a default group. The fix is to
		* get the handle of the default group and add windows to the
		* group.
		*/
		group = GTK.gtk_window_get_group(0);
	}
	if (group != 0) {
		GTK.gtk_window_group_add_window (group, shellHandle);
		if (isModalShell) GTK.gtk_window_set_modal (shellHandle, true);
	} else {
		if (modalGroup != 0) {
			GTK.gtk_window_group_remove_window (modalGroup, shellHandle);
		}
	}
	modalGroup = group;
}

void updateMinimized (boolean minimized) {
	Shell[] shells = getShells ();
	for (int i = 0; i < shells.length; i++) {
		boolean update = false;
		Shell shell = shells[i];
		while (shell != null && shell != this && !shell.isUndecorated ()) {
			shell = (Shell) shell.getParent ();
		}
		if (shell != null && shell != this) update = true;
		if (update) {
			if (minimized) {
				if (shells[i].isVisible ()) {
					shells[i].showWithParent = true;
					GTK.gtk_widget_hide(shells[i].shellHandle);
				}
			} else {
				if (shells[i].showWithParent) {
					shells[i].showWithParent = false;
					GTK.gtk_widget_show(shells[i].shellHandle);
				}
			}
		}
	}
}

@Override
void deregister () {
	super.deregister ();
	Widget disposed = display.removeWidget (shellHandle);
	if(shellHandle != 0 && !(disposed instanceof Shell)) {
		SWT.error(SWT.ERROR_INVALID_RETURN_VALUE, null, ". Wrong widgetTable entry: " + disposed + " removed for shell: " + this + display.dumpWidgetTableInfo());
	}
	if(Display.strictChecks) {
		Shell[] shells = display.getShells();
		for (Shell shell : shells) {
			if(shell == this) {
				SWT.error(SWT.ERROR_INVALID_RETURN_VALUE, null, ". Disposed shell still in the widgetTable: " + this + display.dumpWidgetTableInfo());
			}
		}
	}
}

@Override
public void dispose () {
	/*
	* Note:  It is valid to attempt to dispose a widget
	* more than once.  If this happens, fail silently.
	*/
	if (isDisposed()) return;
	fixActiveShell ();
	if (!OS.isX11() && GTK.GTK_VERSION >= OS.VERSION(3, 20, 0)) {
		if ((style & SWT.ON_TOP) != 0 && (style & SWT.NO_FOCUS) == 0) {
			GTK.gtk_grab_remove(shellHandle);
			GDK.gdk_seat_ungrab(GDK.gdk_display_get_default_seat(
				GDK.gdk_window_get_display(GTK.gtk_widget_get_window(shellHandle))));
		}
	}
	GTK.gtk_widget_hide (shellHandle);
	super.dispose ();
}

/**
 * If the receiver is visible, moves it to the top of the
 * drawing order for the display on which it was created
 * (so that all other shells on that display, which are not
 * the receiver's children will be drawn behind it) and forces
 * the window manager to make the shell active.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @since 2.0
 * @see Control#moveAbove
 * @see Control#setFocus
 * @see Control#setVisible
 * @see Display#getActiveShell
 * @see Decorations#setDefaultButton(Button)
 * @see Shell#open
 * @see Shell#setActive
 */
public void forceActive () {
	checkWidget ();
	bringToTop (true);
}

@Override
Rectangle getBoundsInPixels () {
	checkWidget ();
	int [] x = new int [1], y = new int [1];
	if ((state & Widget.DISPOSE_SENT) == 0) {
		GTK.gtk_window_get_position (shellHandle, x, y);
	} else {
		GDK.gdk_window_get_root_origin(GTK.gtk_widget_get_window(shellHandle), x, y);
	}
	GtkAllocation allocation = new GtkAllocation ();
	GTK.gtk_widget_get_allocation (vboxHandle, allocation);
	int width = allocation.width;
	int height = allocation.height;
	int border = 0;
	if ((style & (SWT.NO_TRIM | SWT.BORDER | SWT.SHELL_TRIM)) == 0 || isCustomResize()) {
		border = GTK.gtk_container_get_border_width (shellHandle);
	}
	return new Rectangle (x [0], y [0], width + trimWidth () + 2*border, height + trimHeight () + 2*border);
}

@Override
void releaseHandle () {
	super.releaseHandle ();
	shellHandle = 0;
}

@Override
void releaseChildren (boolean destroy) {
	Shell [] shells = getShells ();
	for (int i=0; i<shells.length; i++) {
		Shell shell = shells [i];
		if (shell != null && !shell.isDisposed ()) {
			shell.release (false);
		}
	}
	if (toolTips != null) {
		for (int i=0; i<toolTips.length; i++) {
			ToolTip toolTip = toolTips [i];
			if (toolTip != null && !toolTip.isDisposed ()) {
				toolTip.dispose ();
			}
		}
		toolTips = null;
	}
	super.releaseChildren (destroy);
}

@Override
void releaseWidget () {
	Region regionToDispose = null;
	if (originalRegion != null) regionToDispose  = region;
	super.releaseWidget ();
	destroyAccelGroup ();
	display.clearModal (this);
	if (display.activeShell == this) display.activeShell = null;
	if (tooltipsHandle != 0) OS.g_object_unref (tooltipsHandle);
	tooltipsHandle = 0;
	if (group != 0) OS.g_object_unref (group);
	group = modalGroup = 0;
	if (!GTK.GTK3) {
		long /*int*/ window = gtk_widget_get_window (shellHandle);
		GDK.gdk_window_remove_filter(window, display.filterProc, shellHandle);
	}
	lastActive = null;
	if (regionToDispose != null) {
		regionToDispose.dispose();
	}
}

void setToolTipText (long /*int*/ tipWidget, String string) {
	setToolTipText (tipWidget, tipWidget, string);
}

void setToolTipText (long /*int*/ rootWidget, long /*int*/ tipWidget, String string) {
	byte [] buffer = null;
	if (string != null && string.length () > 0) {
		char [] chars = fixMnemonic (string, false);
		buffer = Converter.wcsToMbcs (chars, true);
	}
	long /*int*/ oldTooltip = GTK.gtk_widget_get_tooltip_text (rootWidget);
	boolean same = false;
	if (buffer == null && oldTooltip == 0) {
		same = true;
	} else if (buffer != null && oldTooltip != 0) {
		same = OS.strcmp (oldTooltip, buffer) == 0;
	}
	if (oldTooltip != 0) OS.g_free(oldTooltip);
	if (same) return;

	GTK.gtk_widget_set_tooltip_text (rootWidget, buffer);
}
@Override
Point getWindowOrigin () {
	if (!mapped) {
		/*
		 * Special case: The handle attributes are not initialized until the
		 * shell is made visible, so gdk_window_get_origin () always returns {0, 0}.
		 *
		 * Once the shell is realized, gtk_window_get_position () includes
		 * window trims etc. from the window manager. That's why getLocation ()
		 * is not safe to use for coordinate mappings after the shell has been made visible.
		 */
		return getLocationInPixels ();
	}
	return super.getWindowOrigin( );
}

static long /*int*/ GdkSeatGrabPrepareFunc (long /*int*/ gdkSeat, long /*int*/ gdkWindow, long /*int*/ userData_shellHandle) {
	if (userData_shellHandle != 0) {
		GTK.gtk_widget_show(userData_shellHandle);
	}
	return 0;
}
}
