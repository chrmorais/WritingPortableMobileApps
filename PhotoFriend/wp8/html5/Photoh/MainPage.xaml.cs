/* Copyright (c) 2015 Elvis Pfützenreuter */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Navigation;
using Microsoft.Phone.Controls;
using Microsoft.Phone.Shell;
using Microsoft.Devices;
using System.IO.IsolatedStorage;
using System.Diagnostics;

namespace Photoh
{
    public partial class MainPage : PhoneApplicationPage
    {
        // Url of Home page
        private string MainUri = "/Html/index.html";
        private IsolatedStorageSettings settings;

        // Constructor
        public MainPage()
        {
            InitializeComponent();
            settings = IsolatedStorageSettings.ApplicationSettings;
            if (! settings.Contains("data")) {
                settings.Add("data", "");
            }
        }

        private void Browser_PageLoaded(object sender, NavigationEventArgs e)
        {
            anim.Begin();
            // Browser.Opacity = 1;
        }

        private void Browser_Loaded(object sender, RoutedEventArgs e)
        {
            Browser.IsScriptEnabled = true;
            // Browser.LoadCompleted += new LoadCompletedEventHandler(Browser_PageLoaded);
            Browser.Navigate(new Uri(MainUri, UriKind.Relative));
        }

        // boilerplate
        private void Browser_NavigationFailed(object sender, System.Windows.Navigation.NavigationFailedEventArgs e)
        {
        }

        private void FromJS(object sender, NotifyEventArgs e)
        {
            var opcode = e.Value.Substring(0, 1);

            if (opcode == "s") {
                Debugger.Log(1, "FromJS", "saving\r\n");
                var data = e.Value.Substring(1);
                settings["data"] = data;
            }
            else if (opcode == "l")
            {
                Debugger.Log(1, "FromJS", "loading\r\n");
                string data = settings["data"].ToString();
                Browser.InvokeScript("preload", new string[] { data });
            }
            else
            {
                // Sent for debugging purposes only
                Debugger.Log(1, "FromJS", e.Value + "\r\n");
            }
        }
    }
}
