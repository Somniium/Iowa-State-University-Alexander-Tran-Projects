import tkinter as tk
from tkinter import ttk, scrolledtext, messagebox
import socket
import threading
import time
import math
import importlib
import matplotlib.pyplot as plt
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg
from matplotlib.figure import Figure
import numpy as np
from collections import deque
import json
import re
import random

class CyBotRoverGUI:
    def __init__(self, root):
        self.root = root
        self.root.title("CyBot Rover Control & Live Mapping")
        self.root.geometry("1400x900")
        
        # Socket connection
        self.socket_conn = None
        self.serial_conn = None
        self.connected = False
        self.socket_file = None
        self.read_thread = None
        self.stop_read_thread = threading.Event()
        #show detected objects checkbox
        self.show_detected_objects_var = tk.BooleanVar(value=True)
        # show bump points
        self.show_bump_var = tk.BooleanVar(value=True)
        # show cliff points
        self.show_cliff_var = tk.BooleanVar(value=True)
        # show sensor points
        self.show_sensor_points_var = tk.BooleanVar(value=True)

        # Rover state (matching coords_t from coords.h)
        self.rover_x = 0.0  # meters
        self.rover_y = 0.0  # meters
        self.rover_angle = 0  # degrees (0-359)
        
        # Object storage
        self.detected_objects = []  # List of dictionaries with world_x, world_y, Distance, StartAngle, EndAngle
        self.last_object_id = 0
        
        # Test obstacles (fixed in world coordinates)
        self.test_obstacles = []
        
        # Sensor data storage
        self.sensor_points = deque(maxlen=1000)  # Store (x, y) coordinates

        # Bump and cliff markers
        self.bump_points = []   # list of (x, y)
        self.cliff_points = []  # list of (x, y)        

        self.recv_buffer = ""

        # Scan buffering to avoid spamming raw scan lines in the log
        self._scan_buffering = False
        self._scan_temp = []
        self._scan_last_time = 0
        
        # Manual drive state
        self.manual_mode = False
        self.last_command_time = 0
        
        # Map visualization
        self.map_objects = []  # Matplotlib objects for map
        self.rover_visual = None
        
        # Test mode (for echo server)
        self.test_mode = False
        
        # Create GUI
        self.create_widgets()
        
        # Setup initial map
        self.setup_map()
        
        # Start periodic updates
        self.update_interval = 100  # ms
        self.root.after(self.update_interval, self.periodic_update)
    
    def create_widgets(self):
        """Create all GUI widgets"""
        # Connection frame
        self.create_connection_frame()
        
        # Control frame with relevant buttons
        self.create_control_frame()
        
        # Manual drive frame (WASD controls)
        self.create_manual_drive_frame()
        
        # Live map frame
        self.create_map_frame()
        
        # Status and data display
        self.create_status_frame()
        
        # Log frame
        self.create_log_frame()
    
    def create_connection_frame(self):
        """Create socket connection controls"""
        conn_frame = ttk.LabelFrame(self.root, text="Connection", padding=10)
        conn_frame.grid(row=0, column=0, columnspan=3, sticky="ew", padx=10, pady=5)
        
        ttk.Label(conn_frame, text="Host:").grid(row=0, column=0, sticky="w")
        self.host_var = tk.StringVar(value="192.168.1.1")
        ttk.Entry(conn_frame, textvariable=self.host_var, width=15).grid(row=0, column=1, padx=5)
        
        ttk.Label(conn_frame, text="Port:").grid(row=0, column=2, sticky="w", padx=(20,0))
        self.port_var = tk.StringVar(value="288")
        ttk.Entry(conn_frame, textvariable=self.port_var, width=8).grid(row=0, column=3, padx=5)
        
        self.connect_btn = ttk.Button(conn_frame, text="Connect", command=self.toggle_connection)
        self.connect_btn.grid(row=0, column=4, padx=10)
        
        # Test mode checkbox
        self.test_mode_var = tk.BooleanVar(value=True)  # Default to test mode
        ttk.Checkbutton(conn_frame, text="Test Mode", 
                       variable=self.test_mode_var,
                       command=self.toggle_test_mode).grid(row=0, column=5, padx=10)
        
        # Status label with text variable
        self.status_var = tk.StringVar(value="Disconnected")
        self.status_label = ttk.Label(conn_frame, textvariable=self.status_var, foreground="red")
        self.status_label.grid(row=0, column=6, padx=10)
    
    def create_control_frame(self):
        """Create control buttons matching C code functions"""
        control_frame = ttk.LabelFrame(self.root, text="CyBot Control", padding=10)
        control_frame.grid(row=1, column=0, sticky="nsew", padx=10, pady=5)
        
        # Create a canvas with scrollbar
        canvas = tk.Canvas(control_frame, highlightthickness=0)
        scrollbar = ttk.Scrollbar(control_frame, orient="vertical", command=canvas.yview)
        scrollable_frame = ttk.Frame(canvas)
        
        scrollable_frame.bind(
            "<Configure>",
            lambda e: canvas.configure(scrollregion=canvas.bbox("all"))
        )
        
        canvas.create_window((0, 0), window=scrollable_frame, anchor="nw")
        canvas.configure(yscrollcommand=scrollbar.set)
        
        # Bind mousewheel to canvas
        def _on_mousewheel(event):
            canvas.yview_scroll(int(-1*(event.delta/120)), "units")
        canvas.bind_all("<MouseWheel>", _on_mousewheel)
        
        canvas.pack(side="left", fill="both", expand=True)
        scrollbar.pack(side="right", fill="y")
        
        # Scan functions
        scan_frame = ttk.LabelFrame(scrollable_frame, text="Scan Functions")
        scan_frame.pack(fill="x", pady=5)
        
        ttk.Button(scan_frame, text="Scan Arc (45°)", 
                  command=self.test_scan).pack(fill="x", pady=2)
        
        ttk.Button(scan_frame, text="Full Scan (90°)", 
                  command=self.test_full_scan).pack(fill="x", pady=2)
        
        ttk.Button(scan_frame, text="Object Detection", 
                  command=self.test_object_detection).pack(fill="x", pady=2)
        
        ttk.Button(scan_frame, text="IR Edge Refinement", 
                  command=self.test_ir_refine).pack(fill="x", pady=2)
        
        # Movement functions
        move_frame = ttk.LabelFrame(scrollable_frame, text="Movement")
        move_frame.pack(fill="x", pady=5)
        
        ttk.Button(move_frame, text="Move Forward (1m)", 
                  command=lambda: self.send_command("move_forward 100")).pack(fill="x", pady=2)
        
        ttk.Button(move_frame, text="Move Backward (0.5m)", 
                  command=lambda: self.send_command("move_backward 50")).pack(fill="x", pady=2)
        
        ttk.Button(move_frame, text="Turn Left 90°", 
                  command=lambda: self.send_command("turn_cc 90")).pack(fill="x", pady=2)
        
        ttk.Button(move_frame, text="Turn Right 90°", 
                  command=lambda: self.send_command("turn_cw 90")).pack(fill="x", pady=2)
        
        # System functions
        sys_frame = ttk.LabelFrame(scrollable_frame, text="System")
        sys_frame.pack(fill="x", pady=5)
        
        ttk.Button(sys_frame, text="Enable Manual Mode", 
                  command=self.enable_manual_mode).pack(fill="x", pady=2)
        
        ttk.Button(sys_frame, text="Get Position", 
                  command=self.request_position).pack(fill="x", pady=2)
        
        ttk.Button(sys_frame, text="Reset Position", 
                  command=self.reset_position).pack(fill="x", pady=2)
        
        ttk.Button(sys_frame, text="Clear Map", 
                  command=self.clear_map).pack(fill="x", pady=2)
        
        # Save button
        ttk.Button(sys_frame, text="Save Map Data", 
                  command=self.save_map).pack(fill="x", pady=2)
        
        # show detected objects checkbox
        ttk.Checkbutton(
            scrollable_frame,
            text="Show Detected Objects",
            variable=self.show_detected_objects_var,
            command=self.update_map
        ).pack(anchor="w", pady=4)

        ttk.Checkbutton(
            scrollable_frame,
            text="Show Bump Marker",
            variable=self.show_bump_var,
            command=self.update_map
        ).pack(anchor="w", pady=4)

        ttk.Checkbutton(
            scrollable_frame,
            text="Show Cliff Marker",
            variable=self.show_cliff_var,
            command=self.update_map
        ).pack(anchor="w", pady=4)
        
        ttk.Checkbutton(
            scrollable_frame,
            text="Show Sensor Points",
            variable=self.show_sensor_points_var,
            command=self.update_map
        ).pack(anchor="w", pady=4)
        

        
        # Test buttons
        test_frame = ttk.LabelFrame(scrollable_frame, text="Test Functions")
        test_frame.pack(fill="x", pady=5)
        
        ttk.Button(test_frame, text="Add Test Object", 
                  command=self.add_test_object).pack(fill="x", pady=2)
        
        ttk.Button(test_frame, text="Generate Test Scan", 
                  command=self.generate_test_scan).pack(fill="x", pady=2)
        
        ttk.Button(test_frame, text="Place Wall (1m)", 
                  command=lambda: self.place_test_wall(1.0)).pack(fill="x", pady=2)
        
        ttk.Button(test_frame, text="Place Box (0.5m)", 
                  command=lambda: self.place_test_box(0.5)).pack(fill="x", pady=2)
    
    def create_manual_drive_frame(self):
        """Create WASD manual drive interface"""
        manual_frame = ttk.LabelFrame(self.root, text="Manual Drive (WASD)", padding=10)
        manual_frame.grid(row=2, column=0, sticky="nsew", padx=10, pady=5)
        
        self.manual_status_var = tk.StringVar(value="Manual Mode: OFF")
        ttk.Label(manual_frame, textvariable=self.manual_status_var, 
                 font=("Arial", 12, "bold")).pack(pady=5)
        
        # Key bindings
        key_frame = ttk.Frame(manual_frame)
        key_frame.pack(pady=10)
        
        ttk.Label(key_frame, text="W: Forward", font=("Arial", 10)).grid(row=0, column=1, pady=2)
        ttk.Label(key_frame, text="A: Turn Left", font=("Arial", 10)).grid(row=1, column=0, pady=2)
        ttk.Label(key_frame, text="S: Backward", font=("Arial", 10)).grid(row=1, column=1, pady=2)
        ttk.Label(key_frame, text="D: Turn Right", font=("Arial", 10)).grid(row=1, column=2, pady=2)
        ttk.Label(key_frame, text="Q: Exit Manual", font=("Arial", 10)).grid(row=2, column=1, pady=2)
        
        # Step size control
        step_frame = ttk.Frame(manual_frame)
        step_frame.pack(pady=5)
        
        ttk.Label(step_frame, text="Step (cm):").pack(side="left")
        self.step_size_var = tk.IntVar(value=10)
        self.step_scale = ttk.Scale(step_frame, from_=5, to=50, variable=self.step_size_var,
                                   orient="horizontal", length=150)
        self.step_scale.pack(side="left", padx=5)
        self.step_label = ttk.Label(step_frame, textvariable=self.step_size_var, width=3)
        self.step_label.pack(side="left")
        
        # Bind keyboard
        self.root.bind('<KeyPress>', self.handle_keypress)
    
    def create_map_frame(self):
        """Create live map display"""
        map_frame = ttk.LabelFrame(self.root, text="Live Map - Rover & Objects", padding=10)
        map_frame.grid(row=1, column=1, rowspan=2, sticky="nsew", padx=10, pady=5)
        
        # Create matplotlib figure
        self.fig = Figure(figsize=(8, 8), dpi=100)
        self.ax = self.fig.add_subplot(111)
        
        self.canvas = FigureCanvasTkAgg(self.fig, map_frame)
        self.canvas.get_tk_widget().pack(fill="both", expand=True)
        
        # Map controls
        control_frame = ttk.Frame(map_frame)
        control_frame.pack(fill="x", pady=5)
        
        ttk.Button(control_frame, text="Center on Rover", 
                  command=lambda: self.center_on_rover()).pack(side="left", padx=5)
        
        # Auto-center checkbox
        self.auto_center_var = tk.BooleanVar(value=True)
        ttk.Checkbutton(control_frame, text="Auto-center", 
                       variable=self.auto_center_var).pack(side="left", padx=5)
        
        # Test objects checkbox
        self.show_test_objects_var = tk.BooleanVar(value=True)
        ttk.Checkbutton(control_frame, text="Show Test Objects", 
                       variable=self.show_test_objects_var,
                       command=self.update_map).pack(side="left", padx=5)
    
    def create_status_frame(self):
        """Create status display"""
        status_frame = ttk.LabelFrame(self.root, text="Rover Status", padding=10)
        status_frame.grid(row=1, column=2, rowspan=2, sticky="nsew", padx=10, pady=5)
        
        # Position display
        pos_frame = ttk.LabelFrame(status_frame, text="Position")
        pos_frame.pack(fill="x", pady=5)
        
        self.x_var = tk.StringVar(value="0.00")
        self.y_var = tk.StringVar(value="0.00")
        self.angle_var = tk.StringVar(value="0°")
        
        ttk.Label(pos_frame, text="X:").grid(row=0, column=0, sticky="w")
        ttk.Label(pos_frame, textvariable=self.x_var, font=("Arial", 12)).grid(row=0, column=1, sticky="w")
        
        ttk.Label(pos_frame, text="Y:").grid(row=0, column=2, sticky="w", padx=(10,0))
        ttk.Label(pos_frame, textvariable=self.y_var, font=("Arial", 12)).grid(row=0, column=3, sticky="w")
        
        ttk.Label(pos_frame, text="Angle:").grid(row=1, column=0, sticky="w")
        ttk.Label(pos_frame, textvariable=self.angle_var, font=("Arial", 12)).grid(row=1, column=1, sticky="w")
        
        # Objects count
        obj_frame = ttk.LabelFrame(status_frame, text="Detected Objects")
        obj_frame.pack(fill="x", pady=5)
        
        self.objects_var = tk.StringVar(value="0 objects")
        ttk.Label(obj_frame, textvariable=self.objects_var, font=("Arial", 14, "bold")).pack(pady=10)
        
        # Objects list
        list_frame = ttk.LabelFrame(status_frame, text="Object Details")
        list_frame.pack(fill="both", expand=True, pady=5)
        
        # Create a frame for the listbox and scrollbar
        list_container = ttk.Frame(list_frame)
        list_container.pack(fill="both", expand=True)
        
        # Add scrollbar
        scrollbar = ttk.Scrollbar(list_container)
        scrollbar.pack(side="right", fill="y")
        
        self.objects_listbox = tk.Listbox(list_container, yscrollcommand=scrollbar.set, height=8)
        self.objects_listbox.pack(side="left", fill="both", expand=True)
        
        scrollbar.config(command=self.objects_listbox.yview)
    
    def create_log_frame(self):
        """Create communication log"""
        log_frame = ttk.LabelFrame(self.root, text="Communication Log", padding=10)
        log_frame.grid(row=3, column=0, columnspan=3, sticky="ew", padx=10, pady=5)
        
        self.log_text = scrolledtext.ScrolledText(log_frame, height=6, wrap=tk.WORD)
        self.log_text.pack(fill="both", expand=True)
        
        button_frame = ttk.Frame(log_frame)
        button_frame.pack(fill="x", pady=5)
        
        ttk.Button(button_frame, text="Clear Log", 
                  command=lambda: self.log_text.delete(1.0, tk.END)).pack(side="left")
    
    def setup_map(self):
        """Initialize the live map"""
        self.ax.clear()
        self.ax.set_xlim(-3, 3)
        self.ax.set_ylim(-3, 3)
        self.ax.set_xlabel('X Position (meters)')
        self.ax.set_ylabel('Y Position (meters)')
        self.ax.set_title('CyBot Live Map')
        self.ax.grid(True, alpha=0.3)
        self.ax.set_aspect('equal')
        
        # Draw coordinate axes
        self.ax.axhline(y=0, color='k', linestyle='-', alpha=0.2)
        self.ax.axvline(x=0, color='k', linestyle='-', alpha=0.2)
        
        # Add some test obstacles if in test mode
        if self.test_mode:
            self.add_test_obstacles()
        
        # Draw rover at initial position
        self.draw_rover()
        self.canvas.draw()
    
    def draw_rover(self):
        """Draw rover as a triangle with heading"""
        # Remove old rover if it exists
        if hasattr(self, 'rover_patch') and self.rover_patch:
            try:
                self.rover_patch.remove()
            except:
                pass
        
        size = 0.15  # Rover size in meters
        x, y = self.rover_x, self.rover_y
        angle_rad = math.radians(self.rover_angle)
        
        # CHANGE THIS 
        offset = math.radians(0)

        adjusted_angle = angle_rad + offset


        # Create triangle vertices
        front = (x + size * math.cos(adjusted_angle), 
                 y + size * math.sin(adjusted_angle))
        left = (x + size * math.cos(adjusted_angle + 2.2), 
                y + size * math.sin(adjusted_angle + 2.2))
        right = (x + size * math.cos(adjusted_angle - 2.2), 
                 y + size * math.sin(adjusted_angle - 2.2))
        
        # Draw filled triangle
        self.rover_patch = self.ax.fill([front[0], left[0], right[0]], 
                                       [front[1], left[1], right[1]], 
                                       'blue', alpha=0.7, edgecolor='darkblue', linewidth=2)[0]
        
        # Draw heading arrow
        arrow_length = size * 1.5
        self.ax.arrow(x, y, 
                     arrow_length * math.cos(adjusted_angle), 
                     arrow_length * math.sin(adjusted_angle),
                     head_width=0.05, head_length=0.07,
                     fc='darkblue', ec='darkblue')
    
    def update_map(self):
        """Update the map with all objects and rover position"""
        # Clear axis but keep limits
        xlim, ylim = self.ax.get_xlim(), self.ax.get_ylim()
        
        self.ax.clear()
        self.ax.set_xlim(xlim)
        self.ax.set_ylim(ylim)
        
        # Set labels and grid
        self.ax.set_xlabel('X Position (meters)')
        self.ax.set_ylabel('Y Position (meters)')
        self.ax.set_title(f'CyBot Live Map - {len(self.detected_objects)} objects')
        self.ax.grid(True, alpha=0.3)
        self.ax.set_aspect('equal')
        
        # Draw coordinate axes
        self.ax.axhline(y=0, color='k', linestyle='-', alpha=0.2)
        self.ax.axvline(x=0, color='k', linestyle='-', alpha=0.2)
        
        # Draw test obstacles if enabled
        if self.test_mode and self.show_test_objects_var.get():
            self.draw_test_obstacles()
        
        # Draw sensor points
        if self.show_sensor_points_var.get():
            if self.sensor_points:
                points = list(self.sensor_points)
                xs = [p[0] for p in points]
                ys = [p[1] for p in points]
                self.ax.scatter(xs, ys, c='gray', s=10, alpha=0.3, label='Sensor points')

        # Draw bump markers (blue, a bit larger)
        if self.show_bump_var.get():
            if self.bump_points:
                bx, by = zip(*self.bump_points)
                self.ax.scatter(
                    bx, by,
                    c='blue',
                    s=40,
                    marker='x',
                    alpha=0.9,
                    label='Bump'
                )
            
        # Draw cliff markers (orange squares)
        if self.show_cliff_var.get():
            if self.cliff_points:
                cx, cy = zip(*self.cliff_points)
                self.ax.scatter(
                    cx, cy,
                    c='orange',
                    s=50,
                    marker='s',
                    alpha=0.9,
                    label='Cliff'
                )
        
        # Draw detected objects
        if self.show_detected_objects_var.get():
            for obj in self.detected_objects:
                try:
                    if 'world_x' in obj and 'world_y' in obj:
                        obj_x = obj['world_x']
                        obj_y = obj['world_y']
                    else:
                        # Shouldn't happen with new parser
                        continue
                    
                    # Draw object as circle
                    self.ax.plot(obj_x, obj_y, 'ro', markersize=6, 
                                alpha=0.7, label=f"Object {obj['id']}")
                    
                    # Add text label
                    self.ax.text(obj_x + 0.05, obj_y + 0.05, 
                            f"Obj {obj['id']}\n{obj['Distance']:.1f}m",
                            fontsize=8, bbox=dict(boxstyle="round,pad=0.3",
                                                    facecolor="yellow", alpha=0.7))
                    
                except KeyError:
                    continue
        
        # Draw rover
        self.draw_rover()
        
        # Auto-center if enabled
        if self.auto_center_var.get():
            self.center_on_rover(smooth=True)
        
        # Update legend
        handles, labels = self.ax.get_legend_handles_labels()
        if handles:
            self.ax.legend(handles, labels, loc='upper right')
        
        self.canvas.draw_idle()
    
    def handle_keypress(self, event):
        """Handle manual drive keys"""
        if not self.manual_mode:
            return
        
        current_time = time.time()
        if current_time - self.last_command_time < 0.2:
            return
        
        key = event.char.lower()
        step_cm = self.step_size_var.get()
        
        # Update rover position locally for test mode
        if self.test_mode:
            if key == 'w':
                angle_rad = math.radians(self.rover_angle)
                step_m = step_cm / 100.0
                self.rover_x += step_m * math.cos(angle_rad)
                self.rover_y += step_m * math.sin(angle_rad)
                self.log_message(f"TEST: Moved forward {step_cm}cm")
                
            elif key == 's':
                angle_rad = math.radians(self.rover_angle)
                step_m = step_cm / 100.0
                self.rover_x -= step_m * math.cos(angle_rad)
                self.rover_y -= step_m * math.sin(angle_rad)
                self.log_message(f"TEST: Moved backward {step_cm}cm")
                
            elif key == 'a':
                self.rover_angle = (self.rover_angle + 10) % 360
                self.log_message("TEST: Turned left 10°")
                
            elif key == 'd':
                self.rover_angle = (self.rover_angle - 10) % 360
                self.log_message("TEST: Turned right 10°")
                
            elif key == 'q':
                self.disable_manual_mode()
                
            self.update_position_display()
            self.update_map()
            self.last_command_time = current_time
            return
        
        # Real mode - send commands
        commands = {
            'w': f"move_forward {step_cm}",
            's': f"move_backward {step_cm}",
            'a': "turn_cc 10",
            'd': "turn_cw 10",
            'q': "manual_off"
        }
        
        if key in commands:
            if key == 'q':
                self.disable_manual_mode()
            else:
                self.send_command(commands[key])
                self.log_message(f"Manual: {commands[key]}")
                self.last_command_time = current_time

        if not self.test_mode and key in ('w', 's'):
            step_m = step_cm / 100.0
            direction = 1 if key == 'w' else -1
            angle_rad = math.radians(self.rover_angle)

            self.rover_x += direction * step_m * math.cos(angle_rad)
            self.rover_y += direction * step_m * math.sin(angle_rad)

            self.update_position_display()
            self.update_map()

    
    def enable_manual_mode(self):
        """Enable manual drive mode"""
        self.manual_mode = True
        self.manual_status_var.set("Manual Mode: ON")
        
        if self.test_mode:
            self.log_message("TEST: Manual mode enabled")
        else:
            self.send_command("manual_on")
            self.log_message("Manual mode enabled")
    
    def disable_manual_mode(self):
        """Disable manual drive mode"""
        self.manual_mode = False
        self.manual_status_var.set("Manual Mode: OFF")
        
        if self.test_mode:
            self.log_message("TEST: Manual mode disabled")
        else:
            self.send_command("manual_off")
            self.log_message("Manual mode disabled")
    
    def update_position_display(self):
        """Update position display variables"""
        self.x_var.set(f"{self.rover_x:.2f}")
        self.y_var.set(f"{self.rover_y:.2f}")
        self.angle_var.set(f"{int(self.rover_angle)}°")
    
    def request_position(self):
        """Request position from rover"""
        if self.test_mode:
            # In test mode, just update with current position
            self.log_message(f"TEST: Position is ({self.rover_x:.2f}, {self.rover_y:.2f}, {self.rover_angle}°)")
        else:
            self.send_command("get_position")
    
    def reset_position(self):
        """Reset rover position to origin"""
        self.rover_x = 0.0
        self.rover_y = 0.0
        self.rover_angle = 0
        self.update_position_display()
        self.update_map()
        self.log_message("Position reset to origin")
    
    def clear_map(self):
        """Clear map data"""
        self.detected_objects.clear()
        self.sensor_points.clear()
        self.bump_points.clear()
        self.cliff_points.clear()
        self.update_objects_listbox()
        self.update_map()
        self.log_message("Map cleared")
    
    def center_on_rover(self, smooth=False):
        """Center map on rover position"""
        if self.ax:
            if smooth:
                current_xlim = self.ax.get_xlim()
                current_ylim = self.ax.get_ylim()
                target_x = (self.rover_x - 2, self.rover_x + 2)
                target_y = (self.rover_y - 2, self.rover_y + 2)
                
                # Smooth transition
                new_x = (current_xlim[0] * 0.2 + target_x[0] * 0.8, 
                        current_xlim[1] * 0.2 + target_x[1] * 0.8)
                new_y = (current_ylim[0] * 0.2 + target_y[0] * 0.8,
                        current_ylim[1] * 0.2 + target_y[1] * 0.8)
                
                self.ax.set_xlim(new_x)
                self.ax.set_ylim(new_y)
            else:
                self.ax.set_xlim(self.rover_x - 2, self.rover_x + 2)
                self.ax.set_ylim(self.rover_y - 2, self.rover_y + 2)
            self.canvas.draw_idle()
    
    def update_objects_listbox(self):
        """Update the objects listbox"""
        self.objects_listbox.delete(0, tk.END)
        
        for obj in self.detected_objects:
            try:
                if 'world_x' in obj and 'world_y' in obj:
                    entry = f"Obj {obj['id']}: ({obj['world_x']:.2f}, {obj['world_y']:.2f})"
                elif 'Distance' in obj and 'StartAngle' in obj and 'EndAngle' in obj:
                    width = obj['EndAngle'] - obj['StartAngle']
                    entry = f"Obj {obj['id']}: {obj['Distance']:.2f}m @ {obj['StartAngle']}°-{obj['EndAngle']}°"
                else:
                    entry = f"Obj {obj['id']}: Unknown type"
                self.objects_listbox.insert(tk.END, entry)
            except KeyError:
                continue
        
        self.objects_var.set(f"{len(self.detected_objects)} objects")
    
    def save_map(self):
        """Save map data to JSON file"""
        try:
            map_data = {
                'timestamp': time.strftime('%Y-%m-%d %H:%M:%S'),
                'rover_position': {
                    'x': self.rover_x,
                    'y': self.rover_y,
                    'angle': self.rover_angle
                },
                'detected_objects': self.detected_objects,
                'sensor_points': list(self.sensor_points)
            }
            
            filename = f"cybot_map_{time.strftime('%Y%m%d_%H%M%S')}.json"
            with open(filename, 'w') as f:
                json.dump(map_data, f, indent=2)
            
            self.log_message(f"Map saved to {filename}")
            messagebox.showinfo("Save Successful", f"Map data saved to {filename}")
        except Exception as e:
            self.log_message(f"Error saving map: {e}")
            messagebox.showerror("Save Error", f"Failed to save map: {e}")
    
    # ===== TEST FUNCTIONS =====
    
    def toggle_test_mode(self):
        """Toggle test mode on/off"""
        self.test_mode = self.test_mode_var.get()
        if self.test_mode:
            self.log_message("TEST MODE ENABLED - Using simulated rover")
            self.setup_map()  # Redraw map with test obstacles
        else:
            self.log_message("Test mode disabled")
    
    def add_test_obstacles(self):
        """Add test obstacles to the map (fixed in world coordinates)"""
        # Clear any existing test obstacles
        self.test_obstacles = []
        
        # Add some test obstacles at fixed world coordinates
        obstacles = [
            (1.0, 1.0, 0.2, 'wall'),   # (x, y, size, type)
            (-1.5, 0.5, 0.15, 'box'),
            (0.5, -1.2, 0.25, 'box'),
            (-0.8, -0.8, 0.18, 'wall'),
            (2.0, -1.0, 0.3, 'box'),
        ]
        
        for x, y, size, obs_type in obstacles:
            if obs_type == 'box':
                # Draw as square
                rect = plt.Rectangle((x - size/2, y - size/2), size, size, 
                                    color='gray', alpha=0.5)
                self.ax.add_patch(rect)
                self.test_obstacles.append(('box', rect))
            else:
                # Draw as circle
                circle = plt.Circle((x, y), size, color='gray', alpha=0.5)
                self.ax.add_patch(circle)
                self.test_obstacles.append(('circle', circle))
    
    def draw_test_obstacles(self):
        """Draw test obstacles on map"""
        if not self.test_obstacles:
            self.add_test_obstacles()
        
        for obs_type, obstacle in self.test_obstacles:
            self.ax.add_patch(obstacle)
    
    def place_test_wall(self, distance):
        """Place a wall at a fixed distance in front of rover"""
        if not self.test_mode:
            self.log_message("Enable Test Mode first")
            return
        
        # Calculate wall position in world coordinates
        angle_rad = math.radians(self.rover_angle)
        wall_x = self.rover_x + distance * math.cos(angle_rad)
        wall_y = self.rover_y + distance * math.sin(angle_rad)
        
        # Add as a rectangle (wall)
        wall_length = 0.5
        wall_width = 0.05
        rect = plt.Rectangle((wall_x - wall_length/2, wall_y - wall_width/2), 
                            wall_length, wall_width, 
                            color='darkgray', alpha=0.7, angle=self.rover_angle)
        self.ax.add_patch(rect)
        self.test_obstacles.append(('wall', rect))
        
        # Also add as a detected object
        obj_id = len(self.detected_objects)
        obj_data = {
            'id': obj_id,
            'world_x': wall_x,
            'world_y': wall_y,
            'Distance': distance,
            'StartAngle': -15,  # Small angular width for a wall
            'EndAngle': 15
        }
        self.detected_objects.append(obj_data)
        
        self.update_objects_listbox()
        self.update_map()
        self.log_message(f"TEST: Placed wall at ({wall_x:.2f}, {wall_y:.2f})")
    
    def place_test_box(self, size):
        """Place a box at a random position"""
        if not self.test_mode:
            self.log_message("Enable Test Mode first")
            return
        
        # Place box at random position within 2 meters
        box_x = random.uniform(-2, 2)
        box_y = random.uniform(-2, 2)
        
        # Add as a square
        rect = plt.Rectangle((box_x - size/2, box_y - size/2), size, size, 
                            color='gray', alpha=0.5)
        self.ax.add_patch(rect)
        self.test_obstacles.append(('box', rect))
        
        # Also add as a detected object
        obj_id = len(self.detected_objects)
        
        # Calculate relative position from rover
        dx = box_x - self.rover_x
        dy = box_y - self.rover_y
        distance = math.sqrt(dx**2 + dy**2)
        angle = math.degrees(math.atan2(dy, dx)) - self.rover_angle
        angle = angle % 360
        if angle > 180:
            angle -= 360
        
        obj_data = {
            'id': obj_id,
            'world_x': box_x,
            'world_y': box_y,
            'Distance': distance,
            'StartAngle': angle - 5,  # Box has some angular width
            'EndAngle': angle + 5
        }
        self.detected_objects.append(obj_data)
        
        self.update_objects_listbox()
        self.update_map()
        self.log_message(f"TEST: Placed box at ({box_x:.2f}, {box_y:.2f})")
    
    def test_scan(self):
        """Simulate a scan arc"""
        if not self.test_mode:
            self.send_command("scan_arc_45")
            return
        
        self.log_message("TEST: Simulating 45° scan...")
        
        # Generate simulated scan data based on obstacles
        for angle in range(-22, 23, 2):
            # Calculate distance to nearest obstacle
            distance = self.calculate_test_distance(angle)
            
            # Convert to global coordinates and add to sensor points
            angle_rad = math.radians(angle + self.rover_angle)
            x = self.rover_x + distance * math.cos(angle_rad)
            y = self.rover_y + distance * math.sin(angle_rad)
            self.sensor_points.append((x, y))
            
            # Send simulated sensor data
            self.process_received_data(f"SCAN: angle={angle} dist={distance:.2f}")
        
        self.update_map()
        self.log_message("TEST: Scan complete")
    
    def test_full_scan(self):
        """Simulate a full 90° scan"""
        if not self.test_mode:
            self.send_command("scan_arc_90")
            return
        
        self.log_message("TEST: Simulating 90° scan...")
        
        # Generate simulated scan data
        for angle in range(-45, 46, 2):
            # Calculate distance based on obstacles
            distance = self.calculate_test_distance(angle)
            
            # Convert to global coordinates and add to sensor points
            angle_rad = math.radians(angle + self.rover_angle)
            x = self.rover_x + distance * math.cos(angle_rad)
            y = self.rover_y + distance * math.sin(angle_rad)
            self.sensor_points.append((x, y))
            
            # Send simulated sensor data
            self.process_received_data(f"SCAN: angle={angle} dist={distance:.2f}")
        
        self.update_map()
        self.log_message("TEST: Full scan complete")
    
    def test_object_detection(self):
        """Simulate object detection based on test obstacles"""
        if not self.test_mode:
            self.send_command("detect_objects")
            return
        
        self.log_message("TEST: Simulating object detection...")
        
        # Clear existing objects
        self.detected_objects.clear()
        
        # Detect objects from test obstacles
        for i, (obs_type, obstacle) in enumerate(self.test_obstacles):
            if obs_type == 'circle':
                center = obstacle.center
                radius = obstacle.radius
            elif obs_type in ['box', 'wall']:
                center = (obstacle.get_x() + obstacle.get_width()/2,
                         obstacle.get_y() + obstacle.get_height()/2)
                radius = max(obstacle.get_width(), obstacle.get_height()) / 2
            else:
                continue
            
            # Calculate relative position from rover
            dx = center[0] - self.rover_x
            dy = center[1] - self.rover_y
            distance = math.sqrt(dx**2 + dy**2)
            
            # Only detect if within 3 meters
            if distance <= 3.0:
                angle = math.degrees(math.atan2(dy, dx)) - self.rover_angle
                angle = angle % 360
                if angle > 180:
                    angle -= 360
                
                # Calculate angular width based on size
                angular_width = math.degrees(2 * math.atan(radius / distance))
                
                obj_data = {
                    'id': i,
                    'world_x': center[0],
                    'world_y': center[1],
                    'Distance': distance,
                    'StartAngle': angle - angular_width/2,
                    'EndAngle': angle + angular_width/2
                }
                self.detected_objects.append(obj_data)
                
                # Send simulated object data
                self.process_received_data(f"OBJ: id={i} dist={distance:.2f} start={int(angle-angular_width/2)} end={int(angle+angular_width/2)}")
        
        self.update_objects_listbox()
        self.update_map()
        self.log_message(f"TEST: Detected {len(self.detected_objects)} objects")
    
    def test_ir_refine(self):
        """Simulate IR edge refinement"""
        if not self.test_mode:
            self.send_command("ir_refine")
            return
        
        self.log_message("TEST: Simulating IR edge refinement...")
        
        # Add some IR sensor points near detected objects
        for obj in self.detected_objects:
            if 'world_x' in obj and 'world_y' in obj:
                # Add points around the object for refinement
                for j in range(3):
                    # Add small offset
                    offset_x = random.uniform(-0.1, 0.1)
                    offset_y = random.uniform(-0.1, 0.1)
                    
                    x = obj['world_x'] + offset_x
                    y = obj['world_y'] + offset_y
                    self.sensor_points.append((x, y))
                    
                    # Calculate relative position for IR reading
                    dx = x - self.rover_x
                    dy = y - self.rover_y
                    distance = math.sqrt(dx**2 + dy**2)
                    angle = math.degrees(math.atan2(dy, dx)) - self.rover_angle
                    
                    # Send simulated IR data
                    self.process_received_data(f"IR: {distance*100:.1f}cm @ {angle:.0f}")
        
        self.update_map()
        self.log_message("TEST: IR refinement complete")
    
    def add_test_object(self):
        """Add a test object at fixed world coordinates"""
        if not self.test_mode:
            self.log_message("Enable Test Mode first")
            return
        
        # Generate random position in world coordinates
        world_x = random.uniform(-2, 2)
        world_y = random.uniform(-2, 2)
        
        # Calculate relative position from rover
        dx = world_x - self.rover_x
        dy = world_y - self.rover_y
        distance = math.sqrt(dx**2 + dy**2)
        angle = math.degrees(math.atan2(dy, dx)) - self.rover_angle
        angle = angle % 360
        if angle > 180:
            angle -= 360
        
        # Add as a circle obstacle
        circle = plt.Circle((world_x, world_y), 0.15, color='lightgray', alpha=0.5)
        self.ax.add_patch(circle)
        self.test_obstacles.append(('circle', circle))
        
        # Add as detected object
        obj_id = len(self.detected_objects)
        obj_data = {
            'id': obj_id,
            'world_x': world_x,
            'world_y': world_y,
            'Distance': distance,
            'StartAngle': angle - 8,  # 16 degree width
            'EndAngle': angle + 8
        }
        self.detected_objects.append(obj_data)
        
        # Send simulated object data
        # self.process_received_data(f"OBJ: id={obj_id} dist={distance:.2f} start={int(angle-8)} end={int(angle+8)}")
        
        self.update_objects_listbox()
        self.update_map()
        self.log_message(f"TEST: Added object {obj_id} at ({world_x:.2f}, {world_y:.2f})")
    
    def generate_test_scan(self):
        """Generate random scan data for testing"""
        if not self.test_mode:
            self.log_message("Enable Test Mode first")
            return
        
        self.log_message("TEST: Generating random scan data...")
        
        # Generate random sensor points
        for _ in range(20):
            angle = random.uniform(-45, 45)
            distance = random.uniform(0.3, 2.5)
            
            # Convert to global coordinates
            angle_rad = math.radians(angle + self.rover_angle)
            x = self.rover_x + distance * math.cos(angle_rad)
            y = self.rover_y + distance * math.sin(angle_rad)
            self.sensor_points.append((x, y))
            
            # Randomly choose sensor type
            # sensor_type = random.choice(['PING', 'IR'])
            # self.process_received_data(f"{sensor_type}: {distance*100:.1f}cm @ {angle:.0f}")
        
        self.update_map()
        self.log_message("TEST: Random scan data generated")
    
    def calculate_test_distance(self, angle):
        """Calculate distance to nearest test obstacle"""
        angle_rad = math.radians(angle + self.rover_angle)
        min_distance = 3.0  # Maximum range
        
        # Check each test obstacle
        for obs_type, obstacle in self.test_obstacles:
            if obs_type == 'circle':
                center = obstacle.center
                radius = obstacle.radius
                
                # Ray-circle intersection
                dx = center[0] - self.rover_x
                dy = center[1] - self.rover_y
                
                dot = dx * math.cos(angle_rad) + dy * math.sin(angle_rad)
                discriminant = dot**2 - (dx**2 + dy**2 - radius**2)
                
                if discriminant >= 0:
                    distance = dot - math.sqrt(discriminant)
                    if 0 < distance < min_distance:
                        min_distance = distance
            
            elif obs_type in ['box', 'wall']:
                # Simple approximation: treat as circle for ray casting
                center_x = obstacle.get_x() + obstacle.get_width()/2
                center_y = obstacle.get_y() + obstacle.get_height()/2
                radius = max(obstacle.get_width(), obstacle.get_height()) / 2
                
                dx = center_x - self.rover_x
                dy = center_y - self.rover_y
                
                dot = dx * math.cos(angle_rad) + dy * math.sin(angle_rad)
                discriminant = dot**2 - (dx**2 + dy**2 - radius**2)
                
                if discriminant >= 0:
                    distance = dot - math.sqrt(discriminant)
                    if 0 < distance < min_distance:
                        min_distance = distance
        
        # Add some noise
        min_distance += random.uniform(-0.05, 0.05)
        return max(0.1, min_distance)  # Ensure positive distance
    
    # ===== COMMUNICATION PROTOCOL PARSING =====
    def parse_position(self, data):
        """Parse position data from CyBot"""
        try:
            if "POS:" in data:
                x_match = re.search(r'x=([\d.-]+)', data)
                y_match = re.search(r'y=([\d.-]+)', data)
                angle_match = re.search(r'angle=([\d.-]+)', data)
                
                if x_match and y_match and angle_match:
                    # --- 1) parse raw values from firmware ---
                    x_raw = float(x_match.group(1))
                    y_raw = float(y_match.group(1))
                    raw_angle = float(angle_match.group(1))

                    # --- 2) convert firmware units → GUI units ---
                    # coords.x / coords.y are in millimetres (Roomba distance units),
                    # but the GUI uses meters, so divide by 1000.
                    self.rover_x = x_raw / 1000.0
                    self.rover_y = y_raw / 1000.0

                    # Use CyBot angle as truth, wrap into [0, 360)
                    self.rover_angle = raw_angle % 360.0

                    # --- 3) log in meters for sanity ---
                    self.log_message(
                        f"POS from CyBot → x={self.rover_x:.3f} m, "
                        f"y={self.rover_y:.3f} m, angle={self.rover_angle:.2f}°"
                    )

                    # --- 4) force UI refresh from real pose ---
                    self.update_position_display()
                    self.update_map()
                    return True
        except Exception as e:
            self.log_message(f"Position parse error: {e}")
        return False

    def parse_object(self, data):
        """Parse object detection data"""
        m = re.search(
            r'obj\s+\d+:\s*start=([-\d.]+),\s*end=([-\d.]+),\s*dist=([-\d.]+)\s*m',
            data
        )
        if not m:
            return False

        start = float(m.group(1))
        end = float(m.group(2))
        dist = float(m.group(3))

        # --- wrap-safe midpoint ---
        span = (end - start) % 360.0
        mid = (start + span / 2.0)

  
        world_angle = (mid)
        angle_rad = math.radians(world_angle)

        # Calculate object position
        ox = self.rover_x + dist * math.cos(angle_rad)
        oy = self.rover_y + dist * math.sin(angle_rad)

        # Create object data
        obj_id = len(self.detected_objects)
        obj_data = {
            'id': obj_id,
            'world_x': ox,
            'world_y': oy,
            'Distance': dist,
            'StartAngle': start,
            'EndAngle': end,
            'mid_angle': mid
        }
        
        # Add to detected objects
        self.detected_objects.append(obj_data)
        
        self.log_message(
            f"Object {obj_id} detected at ({ox:.2f}, {oy:.2f}) from mid={mid:.1f}°, world={world_angle:.1f}°"
        )
        
        # Update the objects listbox and map
        self.update_objects_listbox()
        self.update_map()
        return True
    """
    def parse_sensor_data(self, data):
        try:
            if "PING:" in data and "cm" in data:
                ping_match = re.search(r'PING:\s*([\d.]+)\s*cm', data)
                angle_match = re.search(r'@\s*([\d.-]+)', data)
                if ping_match:
                    distance_cm = float(ping_match.group(1))
                    angle = float(angle_match.group(1)) if angle_match else 0
                    distance_m = distance_cm / 100.0
                    
                    angle_rad = math.radians(angle + self.rover_angle)
                    x = self.rover_x + distance_m * math.cos(angle_rad)
                    y = self.rover_y + distance_m * math.sin(angle_rad)
                    
                    self.sensor_points.append((x, y))
                    return True
            
            elif "IR:" in data and "cm" in data:
                ir_match = re.search(r'IR:\s*([\d.]+)\s*cm', data)
                angle_match = re.search(r'@\s*([\d.-]+)', data)
                if ir_match:
                    distance_cm = float(ir_match.group(1))
                    angle = float(angle_match.group(1)) if angle_match else 0
                    distance_m = distance_cm / 100.0
                    
                    angle_rad = math.radians(angle + self.rover_angle)
                    x = self.rover_x + distance_m * math.cos(angle_rad)
                    y = self.rover_y + distance_m * math.sin(angle_rad)
                    
                    self.sensor_points.append((x, y))
                    return True
            
            elif "BUMP:" in data:
                bump_offset_m = 0.10  # s cm in front of the bot
                angle_rad = math.radians(self.rover_angle)
                bx = self.rover_x + bump_offset_m * math.cos(angle_rad)
                by = self.rover_y + bump_offset_m * math.sin(angle_rad)
                
                self.bump_points.append((bx, by))
                self.log_message(
                    f"Bump detected; marker at ({bx:.2f}, {by:.2f})"
                )
                return True
            
            elif "CLIFF:" in data:
                cx, cy = self.rover_x, self.rover_y
                self.cliff_points.append((cx, cy))
                self.log_message(
                    f"Cliff detected; marker at ({cx:.2f}, {cy:.2f})"
                )
                return True
        
        except Exception as e:
            self.log_message(f"Sensor parse error: {e}")
        return False
    """

    def parse_sensor_data(self, data):
        """Parse generic sensor data: PING, IR, BUMP, CLIFF, etc."""
        try:
            # ---------------- PING ----------------
            if "PING:" in data and "@" in data:
                dist_match = re.search(r'PING:\s*([\d.]+)\s*m', data)
                angle_match = re.search(r'@\s*([\d.-]+)', data)
                if dist_match and angle_match:
                    distance_m = float(dist_match.group(1))
                    angle = float(angle_match.group(1))

                    relative = angle
                    world_angle = self.rover_angle + relative
                    angle_rad = math.radians(world_angle)

                    x = self.rover_x + distance_m * math.cos(angle_rad)
                    y = self.rover_y + distance_m * math.sin(angle_rad)
                    self.sensor_points.append((x, y))
                    self.update_map()
                    return True

            # ---------------- IR ----------------
            if "IR:" in data and "@" in data:
                dist_match = re.search(r'IR:\s*([\d.]+)\s*m', data)
                angle_match = re.search(r'@\s*([\d.-]+)', data)
                if dist_match and angle_match:
                    distance_m = float(dist_match.group(1))
                    angle = float(angle_match.group(1))

                    relative = angle
                    world_angle = self.rover_angle + relative
                    angle_rad = math.radians(world_angle)

                    x = self.rover_x + distance_m * math.cos(angle_rad)
                    y = self.rover_y + distance_m * math.sin(angle_rad)
                    self.sensor_points.append((x, y))
                    self.update_map()
                    return True

            # ---------------- BUMP ----------------
            # firmware prints: "BUMP DETECTED"
            if "BUMP" in data:
                bump_offset_m = 0.20   # 4 cm in front of bot
                angle_rad = math.radians(self.rover_angle)
                bx = self.rover_x + bump_offset_m * math.cos(angle_rad)
                by = self.rover_y + bump_offset_m * math.sin(angle_rad)

                self.bump_points.append((bx, by))
                self.log_message(
                    f"Bump detected; marker at ({bx:.2f}, {by:.2f})"
                )
                backward_distance = 0.05  # 5 cm
                self.rover_x -= backward_distance * math.cos(angle_rad)
                self.rover_y -= backward_distance * math.sin(angle_rad)
                self.update_position_display
                self.update_map()
                return True

            # ---------------- CLIFF ----------------
            # firmware prints: "CLIFF DETECTED"
            if "CLIFF DETECTED" in data:
                cx, cy = self.rover_x, self.rover_y

                angle_rad = math.radians(self.rover_angle)
                self.cliff_points.append((cx, cy))
                self.log_message(
                    f"Cliff detected; marker at ({cx:.2f}, {cy:.2f})"
                )
                backward_distance = 0.10  # Would be 0.05 but for visibility do 0.10, 5 cm
                self.rover_x -= backward_distance * math.cos(angle_rad)
                self.rover_y -= backward_distance * math.sin(angle_rad)
                self.update_position_display
                self.update_map()
                return True

        except Exception as e:
            self.log_message(f"Sensor parse error: {e}")
        return False


    def parse_scan_data(self, data):
        """Parse scan data from cyBot_ScanArc"""
        try:
            # Single-line tagged scan, e.g. "SCAN: angle=85 dist=0.30"
            if "SCAN:" in data:
                angle_match = re.search(r'angle=([\d.-]+)', data)
                dist_match = re.search(r'dist=([\d.]+)', data)
                if angle_match and dist_match:
                    angle = float(angle_match.group(1))
                    distance = float(dist_match.group(1))

                    # Scan angles are relative with 90° ≈ straight ahead of rover.
                    relative = angle
                    world_angle = relative
                    angle_rad = math.radians(world_angle)
                    x = self.rover_x + distance * math.cos(angle_rad)
                    y = self.rover_y + distance * math.sin(angle_rad)
                    self.sensor_points.append((x, y))
                    return True

            # Firmware sometimes prints raw lines like: " 12        0.35" (angle distance)
            m = re.match(r'^\s*([+-]?\d+)\s+([\d.]+)\s*$', data)
            if m:
                angle = float(m.group(1))
                distance = float(m.group(2))

                # Same 90°-is-front convention.
                relative = angle
                world_angle = relative
                angle_rad = math.radians(world_angle)
                x = self.rover_x + distance * math.cos(angle_rad)
                y = self.rover_y + distance * math.sin(angle_rad)
                self.sensor_points.append((x, y))
                return True
        except Exception as e:
            self.log_message(f"Scan parse error: {e}")
        return False
    
    def process_received_data(self, data):
        # Detect start of a scan block
        if any(h in data for h in ("Angle(deg)", "FINAL SCAN", "Cleaned Data")):
            self._scan_buffering = True
            self._scan_temp = []
            self._scan_last_time = time.time()
            self.log_message("RECV: <scan started>")
            return

        # If buffering and the line looks like an angle-distance pair, collect it quietly
        m = re.match(r'^\s*([+-]?\d+)\s+([\d.]+)\s*$', data)
        if self._scan_buffering and m:
            angle = float(m.group(1))
            distance = float(m.group(2))
            self._scan_temp.append((angle, distance))
            self._scan_last_time = time.time()
            return
        
        if "FINAL OBJ DETECTION" in data:
            self.log_message("Final detection pass complete.")
            return
        
        # If we were buffering and now saw a non-angle line, flush the buffer
        if self._scan_buffering:
            # Convert collected scan lines into sensor points
            for angle, distance in self._scan_temp:
                relative = angle
                world_angle = relative
                angle_rad = math.radians(world_angle)
                x = self.rover_x + distance * math.cos(angle_rad)
                y = self.rover_y + distance * math.sin(angle_rad)
                self.sensor_points.append((x, y))
            count = len(self._scan_temp)
            self._scan_buffering = False
            self._scan_temp = []
            self.log_message(f"RECV: <scan complete> {count} points")

        # Normal processing and logging for non-scan lines
        self.log_message(f"RECV: {data}")

        if self.parse_position(data):
            return
        elif self.parse_object(data):
            return
        elif self.parse_sensor_data(data):
            return
        elif self.parse_scan_data(data):
            return
        self.log_message(f"Unparsed: {data}")
    
    def send_command(self, cmd):
        """Send command to rover or test environment"""
        # Shared mapping for both serial and socket
        token = cmd.strip().split()[0]
        mapping = {
            'move_forward': b'w',
            'move_backward': b's',
            'turn_cc':      b'a',
            'turn_cw':      b'd',

            # scans (match  firmware: 'e' and 'f')
            'scan_arc_45':  b'e',
            'scan_arc_90':  b'f',

            # position
            'get_position': b'p',

            # modes / misc
            'manual_on':    b'm',
            'manual_off':   b'q',
        }

        # Decide what bytes to send
        to_send = None
        if token in mapping:
            to_send = mapping[token]
        else:
            # testing / raw command path
            stripped = cmd.strip()
            if len(stripped) == 1:
                # single-char raw command, e.g. "w", "e", etc.
                to_send = stripped.encode()
            else:
                # send raw line with newline for whatever bridge expects
                to_send = (stripped + "\n").encode()

        # SERIAL path
        if self.serial_conn:
            try:
                self.serial_conn.write(to_send)
                self.log_message(f"SENT (serial): {to_send!r} -> {cmd}")
            except Exception as e:
                self.log_message(f"Serial send error: {e}")
                self.disconnect()

        # SOCKET/WIFI path
        elif self.connected and self.socket_conn:
            try:
                self.socket_conn.sendall(to_send)
                self.log_message(f"SENT: {cmd} -> {to_send!r}")
            except Exception as e:
                self.log_message(f"Send error: {e}")
                self.disconnect()

        # TEST MODE only (no real IO)
        elif self.test_mode:
            self.log_message(f"TEST: Would send: {cmd}")

    def toggle_connection(self):
        """Toggle connection to rover"""
        if not self.connected:
            if self.test_mode:
                self.connected = True
                self.connect_btn.config(text="Disconnect")
                self.status_var.set("Connected (Test Mode)")
                self.status_label.config(foreground="green")
                self.log_message(f"Connected to TEST MODE")
                self._last_position_request = time.time()
                return
            
            try:
                host = self.host_var.get().strip()
                port = int(self.port_var.get())

                # Auto-detect serial when host is a COM port (Windows) or tty path
                if host.upper().startswith('COM') or host.lower().startswith('/dev/tty'):
                    try:
                        serial_mod = importlib.import_module('serial')
                    except Exception as e:
                        self.log_message(f"pyserial import error: {e}")
                        messagebox.showerror("Dependency Error", "pyserial is required for serial connections. Install with 'pip install pyserial'.")
                        return

                    try:
                        self.serial_conn = serial_mod.Serial(port=host, baudrate=port, timeout=0.5)
                        self.connected = True
                        self.connect_btn.config(text="Disconnect")
                        self.status_var.set(f"Serial:{host}@{port}")
                        self.status_label.config(foreground="green")
                        self.log_message(f"Connected to serial {host} @ {port}")

                        self.stop_read_thread.clear()
                        self.read_thread = threading.Thread(target=self.read_serial, daemon=True)
                        self.read_thread.start()
                        # Avoid sending immediate position request right after connect
                        self._last_position_request = time.time()
                    except Exception as e:
                        self.log_message(f"Serial connection failed: {e}")
                        messagebox.showerror("Connection Error", f"Serial connection failed: {e}")
                        return
                else:
                    # Treat as TCP socket
                    self.socket_conn = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                    self.socket_conn.settimeout(5.0)
                    self.socket_conn.connect((host, port))
                    self.socket_conn.settimeout(0.5)

                    self.connected = True
                    self.connect_btn.config(text="Disconnect")
                    self.status_var.set("Connected")
                    self.status_label.config(foreground="green")
                    self.log_message(f"Connected to {host}:{port}")

                    self.stop_read_thread.clear()
                    self.read_thread = threading.Thread(target=self.read_socket, daemon=True)
                    self.read_thread.start()
                    # Avoid sending immediate position request right after connect
                    self._last_position_request = time.time()
                
            except socket.timeout:
                self.log_message("Connection timeout")
                messagebox.showerror("Connection Error", "Connection timeout")
            except ConnectionRefusedError:
                self.log_message("Connection refused")
                messagebox.showerror("Connection Error", "Connection refused.")
            except Exception as e:
                self.log_message(f"Connection failed: {e}")
                messagebox.showerror("Connection Error", f"Failed to connect: {str(e)}")
        else:
            self.disconnect()
    
    def read_socket(self):
        """Read data from socket"""
        try:
            while self.connected and not self.stop_read_thread.is_set():
                try:
                    data = self.socket_conn.recv(4096)
                    if not data:
                        break
                    
                    lines = data.decode('utf-8', errors='ignore').split('\n')
                    for line in lines:
                        line = line.strip()
                        if line:
                            self.root.after(0, lambda data=line: self.process_received_data(data))
                    
                except socket.timeout:
                    continue
                except socket.error as e:
                    if self.connected:
                        self.log_message(f"Socket error in read: {e}")
                    break
                except Exception as e:
                    if self.connected:
                        self.log_message(f"Read error: {e}")
                    break
                    
                time.sleep(0.01)
                
        except Exception as e:
            self.log_message(f"Socket thread error: {e}")
        finally:
            if self.connected:
                self.root.after(0, self.disconnect)

    def read_serial(self):
        """Read data from serial connection"""
        try:
            while self.connected and not self.stop_read_thread.is_set():
                try:
                    if not self.serial_conn:
                        break
                    raw = self.serial_conn.readline()
                    if not raw:
                        time.sleep(0.01)
                        continue
                    try:
                        line = raw.decode('utf-8', errors='ignore').strip()
                    except AttributeError:
                        # pyserial may return str already
                        line = str(raw).strip()

                    if line:
                        self.root.after(0, lambda data=line: self.process_received_data(data))

                except Exception as e:
                    if self.connected:
                        self.log_message(f"Serial read error: {e}")
                    break

                time.sleep(0.01)

        except Exception as e:
            self.log_message(f"Serial thread error: {e}")
        finally:
            if self.connected:
                self.root.after(0, self.disconnect)
    
    def disconnect(self):
        """Disconnect from rover"""
        if self.connected:
            self.stop_read_thread.set()
            
            if not self.test_mode:
                try:
                    self.send_command("quit")
                    time.sleep(0.1)
                except:
                    pass
                # Close socket if present
                if self.socket_conn:
                    try:
                        self.socket_conn.shutdown(socket.SHUT_RDWR)
                    except:
                        pass
                    try:
                        self.socket_conn.close()
                    except:
                        pass
                    self.socket_conn = None

                # Close serial if present
                if self.serial_conn:
                    try:
                        self.serial_conn.close()
                    except:
                        pass
                    self.serial_conn = None

                if self.socket_file:
                    try:
                        self.socket_file.close()
                    except:
                        pass
                    self.socket_file = None

                if self.read_thread and self.read_thread.is_alive():
                    self.read_thread.join(timeout=1.0)
            
            self.connected = False
            self.connect_btn.config(text="Connect")
            self.status_var.set("Disconnected")
            self.status_label.config(foreground="red")
            self.log_message("Disconnected")
    
    def periodic_update(self):
        """Periodic GUI updates"""
        if hasattr(self, 'auto_center_var') and self.auto_center_var.get():
            self.center_on_rover(smooth=True)
        
        if self.connected and not self.manual_mode:
            current_time = time.time()
            if not hasattr(self, '_last_position_request'):
                # initialize to now so we don't immediately request on connect
                self._last_position_request = current_time

            if current_time - self._last_position_request > 3:
                self.request_position()
                self._last_position_request = current_time
        
        self.root.after(self.update_interval, self.periodic_update)
    
    def log_message(self, message):
        """Add message to log"""
        timestamp = time.strftime("%H:%M:%S")
        self.log_text.insert(tk.END, f"[{timestamp}] {message}\n")
        self.log_text.see(tk.END)


def main():
    root = tk.Tk()
    app = CyBotRoverGUI(root)
    
    # Configure grid weights
    root.grid_columnconfigure(0, weight=0)
    root.grid_columnconfigure(1, weight=3)
    root.grid_columnconfigure(2, weight=1)
    root.grid_rowconfigure(1, weight=1)
    root.grid_rowconfigure(2, weight=1)
    root.grid_rowconfigure(3, weight=0)
    
    def on_closing():
        app.disconnect()
        root.destroy()
    
    root.protocol("WM_DELETE_WINDOW", on_closing)
    root.mainloop()

if __name__ == "__main__":
    main()