using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Navigation;
using Microsoft.Phone.Controls;
using Microsoft.Phone.Shell;
using PhotoN.Resources;
using System.IO.IsolatedStorage;
using System.Diagnostics;

namespace PhotoN
{
    public partial class MainPage : PhoneApplicationPage
    {
        private IsolatedStorageSettings settings;
        private string uri = "/Html/index.html";
        private string near = "";
        private string far = "";

        // Constructor
        public MainPage()
        {
            InitializeComponent();

            settings = IsolatedStorageSettings.ApplicationSettings;
            if (! settings.Contains("data")) {
                    settings.Add("data", "");
            }
        }

        private void Engine_PageLoaded(object sender, NavigationEventArgs e)
        {
            Engine.InvokeScript("model_init", new string[] { });
        }

        private void Engine_Failed(object sender, System.Windows.Navigation.NavigationFailedEventArgs e)
        {
            Debugger.Log(1, "Main", "Engine Failed");
        }

        private void Engine_Loaded(object sender, RoutedEventArgs e)
        {
            Engine.IsScriptEnabled = true;
            Engine.Navigate(new Uri(uri, UriKind.Relative));
        }

        private void FromJS(object sender, NotifyEventArgs e)
        {
            Debugger.Log(1, "FromJS", e.Value + "\r\n");

            var opcode = e.Value.Substring(0, 1);

            if (opcode == "s")
            {
                settings["data"] = e.Value.Substring(1);
            }
            else if (opcode == "l")
            {
                string data = settings["data"].ToString();
                Engine.InvokeScript("preload", new string[] { data });
            }
            else if (opcode == "o")
            {
                string[] parts = e.Value.Substring(1).Split("@".ToCharArray());
                string name = parts[0];
                string value = parts[1];
                int i;
                try
                {
                    i = Convert.ToInt32(parts[2]);
                }
                catch (FormatException)
                {
                    // depth of field sends decimal values
                    i = 0; 
                }
                output(name, value, i);
            }
            else
            {
                Debugger.Log(1, "FromJS", e.Value + "\r\n");
            }
        }

        private void input(string name, string op)
        {
            Engine.InvokeScript("model_input", new string[] { name, op });
        }

        public void output(string name, string value, int index)
        {
            if (name == "near")
            {
                near = value;
                TextBlock x = FindName("dof") as TextBlock;
                x.Text = near + " to " + far;
            }
            else if (name == "far")
            {
                far = value;
                TextBlock x = FindName("dof") as TextBlock;
                x.Text = near + " to " + far;
            }
            else
            {
                TextBlock x = FindName(name) as TextBlock;
                x.Text = value;
            }
        }

        private void ev_minus(object sender, RoutedEventArgs e)
        {
            input("ev", "minus");
        }

        private void iso_minus(object sender, RoutedEventArgs e)
        {
            input("iso", "minus");
        }

        private void shutter_minus(object sender, RoutedEventArgs e)
        {
            input("shutter", "minus");
        }

        private void aperture_minus(object sender, RoutedEventArgs e)
        {
            input("aperture", "minus");
        }

        private void focallength_minus(object sender, RoutedEventArgs e)
        {
            input("focallength", "minus");
        }

        private void distance_minus(object sender, RoutedEventArgs e)
        {
            input("distance", "minus");
        }

        private void ev_plus(object sender, RoutedEventArgs e)
        {
            input("ev", "plus");
        }

        private void iso_plus(object sender, RoutedEventArgs e)
        {
            input("iso", "plus");
        }

        private void shutter_plus(object sender, RoutedEventArgs e)
        {
            input("shutter", "plus");
        }

        private void aperture_plus(object sender, RoutedEventArgs e)
        {
            input("aperture", "plus");
        }

        private void focallength_plus(object sender, RoutedEventArgs e)
        {
            input("focallength", "plus");
        }

        private void distance_plus(object sender, RoutedEventArgs e)
        {
            input("distance", "plus");
        }
    }
}