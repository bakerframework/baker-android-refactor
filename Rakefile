require 'yaml'
require 'pry'
require_relative 'setup/helper'

include Setup::Helper

# setup tasks
namespace :setup do
  
  task :app, [:access_token] do |_, args|
    args.with_defaults(:access_token => false)
    
    # initialize api
    init_rest()
    
    # retrieve access key
    @access_token = args[:access_token] || ask("MagLoft Access Token")
    
    # get user info
    user_id = @access_token.split("$").first
    user = api_get(:portal, 'me')
    
    # get magazine
    magazines = api_get(:portal, 'magazines')
    if magazines.length == 1
      magazine = magazines[0]
    else
      magazine = ask_resource_choice("Select a magazine", magazines, 'app_id', 'title')
    end
    puts "-- selecting magazine: #{magazine["app_id"]}"
    
    # generate friendly app id
    friendly_app_id = magazine["app_id"].gsub(/\-/, '')
    
    # get magazine and user properties
    magazine_properties = api_get(:portal, "magazines/#{magazine["id"]}/properties")
    
    # android manifest
    xml_file_inject("baker/src/main/AndroidManifest.xml", "gcm_category_name", "<category android:name=\"#{friendly_app_id}\" />")
    xml_file_inject("baker/src/main/AndroidManifest.xml", "permission_c2d", "<permission android:name=\"#{friendly_app_id}.permission.C2D_MESSAGE\" android:protectionLevel=\"signature\" />")
    xml_file_inject("baker/src/main/AndroidManifest.xml", "uses_permission_c2d", "<uses-permission android:name=\"#{friendly_app_id}.permission.C2D_MESSAGE\" />")    
    
    # build.gradle
    gradle_property("gradle.properties", "application-id", friendly_app_id)
    
    # strings
    xml_file_inject("baker/src/main/res/values/strings.xml", "app_id", "<string name=\"app_id\">#{magazine["app_id"]}</string>")
    xml_file_inject("baker/src/main/res/values/strings.xml", "app_name", "<string name=\"app_name\">#{magazine["title"]}</string>")
    xml_file_inject("baker/src/main/res/values/strings.xml", "parse_application_id", "<string name=\"parse_application_id\">#{magazine["parse_application_id"]}</string>")
    xml_file_inject("baker/src/main/res/values/strings.xml", "parse_client_key", "<string name=\"parse_client_key\">#{magazine["parse_client_key"]}</string>")
    xml_file_inject("baker/src/main/res/values/strings.xml", "google_analytics_tracking_id", "<string name=\"google_analytics_tracking_id\">#{magazine_properties["google_tracking_code"]}</string>")
    subscriptions = []
    subscriptions.push("<item>#{friendly_app_id}.sub.#{magazine_properties["dbm_subscription_duration"]}</item>")  if magazine_properties["dbm_subscription_price"] != "0"
    if not magazine_properties["dbm_subscription_duration_2"].nil? and 
       not magazine_properties["dbm_subscription_price_2"].nil? and 
       not magazine_properties["dbm_subscription_trial_period_2"].nil? and 
       not magazine_properties["dbm_subscription_opt_in_offer_2"].nil?
      subscriptions.push("<item>#{friendly_app_id}.sub.#{magazine_properties["dbm_subscription_duration_2"]}</item>")  if magazine_properties["dbm_subscription_price_2"] != "0"
    end
    
    xml_file_inject("baker/src/main/res/values/strings.xml", "google_play_subscription_ids", "<string-array name=\"google_play_subscription_ids\">#{subscriptions.join("")}</string-array>")
    
    if magazine_properties["shelf_background_type"] == "asset_shelf_background_pattern"
      place_cdn_image(magazine_properties["asset_shelf_background_pattern"], [
        {path: "baker/src/main/assets/img/shelf-bg.png"}
      ])
      css_file_inject("baker/src/main/assets/background.html", "shelf_background", "background: url(img/shelf-bg.png) center center repeat;")
    elsif magazine_properties["shelf_background_type"] == "shelf_background_gradien_color"
      css_file_inject("baker/src/main/assets/background.html", "shelf_background", "background: linear-gradient(#{magazine_properties["shelf_background_gradient_color_1"]}, #{magazine_properties["shelf_background_gradient_color_2"]});")
    else
      css_file_inject("baker/src/main/assets/background.html", "shelf_background", "background: #{magazine_properties["shelf_background_solid_color"]};")
    end
    
    # App Logo
    place_cdn_image(magazine_properties["asset_large_app_icon"], [
      {path: "baker/src/main/assets/img/logo.png", width: 1024, height: 1024},
      {path: "baker/src/main/res/drawable-xxhdpi/logo.png", width: 648, height: 648},
      {path: "baker/src/main/res/drawable-xhdpi/logo.png", width: 432, height: 432},
      {path: "baker/src/main/res/drawable-hdpi/logo.png", width: 324, height: 324},
      {path: "baker/src/main/res/drawable-mdpi/logo.png", width: 216, height: 216}
    ])
    
    # App Icons / Launcher
    place_cdn_image(magazine_properties["asset_large_app_icon"], [
      {path: "baker/src/main/res/drawable-xxhdpi/ic_launcher.png", width: 144, height: 144, transform: :icon, radius: 8, shadow: 2, padding: 8},
      {path: "baker/src/main/res/drawable-xhdpi/ic_launcher.png", width: 96, height: 96},
      {path: "baker/src/main/res/drawable-hdpi/ic_launcher.png", width: 72, height: 72},
      {path: "baker/src/main/res/drawable-mdpi/ic_launcher.png", width: 48, height: 48}
    ])
    
    # Info dialog / Feature Graphic
    place_cdn_image(magazine_properties["asset_feature_graphic"], [
      {path: "baker/src/main/assets/img/splash.png", width: 1024, height: 500}
    ])

    # Header images
    if magazine_properties["shelf_header_type"] == "asset_shelf_header_full_sized_image"
      place_cdn_image(magazine_properties["asset_shelf_header_full_sized_image"], [
        {path: "baker/src/main/assets/img/header.png", width: 2048, height: 430}
      ])
      css_file_inject("baker/src/main/assets/header.html", "header_background_size", "cover")
    else
      place_cdn_image(magazine_properties["asset_shelf_header_centered_logo"], [
        {path: "baker/src/main/assets/img/header.png", width: 1024, height: 1024}
      ])
      css_file_inject("baker/src/main/assets/header.html", "header_background_size", "contain")
    end
    
  end
  
  task :clean, [:app_id] do |_, args|
    args.with_defaults(:app_id => "com.magloft.demo")
    
    # android manifest
    xml_file_inject("baker/src/main/AndroidManifest.xml", "gcm_category_name", "<category android:name=\"#{args[:app_id]}\" />")
    xml_file_inject("baker/src/main/AndroidManifest.xml", "permission_c2d", "<permission android:name=\"#{args[:app_id]}.permission.C2D_MESSAGE\" android:protectionLevel=\"signature\" />")
    xml_file_inject("baker/src/main/AndroidManifest.xml", "uses_permission_c2d", "<uses-permission android:name=\"#{args[:app_id]}.permission.C2D_MESSAGE\" />")
    
    # build.gradle
    gradle_property("gradle.properties", "application-id", "com.magloft.demo")
    
    # strings
    xml_file_inject("baker/src/main/res/values/strings.xml", "app_id", "<string name=\"app_id\">#{args[:app_id]}</string>")
    xml_file_inject("baker/src/main/res/values/strings.xml", "app_name", "<string name=\"app_name\">#{args[:app_id]}</string>")
    xml_file_inject("baker/src/main/res/values/strings.xml", "parse_application_id", "<string name=\"parse_application_id\"></string>")
    xml_file_inject("baker/src/main/res/values/strings.xml", "parse_client_key", "<string name=\"parse_client_key\"></string>")
    xml_file_inject("baker/src/main/res/values/strings.xml", "google_analytics_tracking_id", "<string name=\"google_analytics_tracking_id\"></string>")
    xml_file_inject("baker/src/main/res/values/strings.xml", "google_play_subscription_ids", "<string-array name=\"google_play_subscription_ids\"></string-array>")    
  end
  
end

device_presets = {
  ldpi_s:   {width:  240, height:  320, dpi: 120},
  ldpi_m:   {width:  240, height:  400, dpi: 120},
  ldpi_l:   {width:  480, height:  800, dpi: 120},
  mdpi_m:   {width:  320, height:  480, dpi: 160},
  mdpi_l:   {width:  480, height:  800, dpi: 160},
  mdpi_xl:  {width:  800, height: 1280, dpi: 160},
  hdpi_s:   {width:  480, height:  640, dpi: 240},
  hdpi_m:   {width:  480, height:  800, dpi: 240},
  hdpi_xl:  {width: 1152, height: 1536, dpi: 240},
  xhdpi_m:  {width:  640, height:  960, dpi: 320},
  xhdpi_xl: {width: 1536, height: 2048, dpi: 320}
}

namespace :adb do
  
  namespace :screen do
  
    task :list do
      device_presets.each do |key, specs|
        name = key.to_s.rjust(10, ' ')
        puts "#{name}: ( width:#{specs[:width].to_s.rjust(4, ' ')} height:#{specs[:height].to_s.rjust(4, ' ')} dpi:#{specs[:dpi].to_s.rjust(3, ' ')} )"
      end
    end
  
    task :set, [:device] do |_, args|
      args.with_defaults(:device => "default")
      
      # get default stats
      size_result = `adb shell wm size |grep "Physical"`.split(": ").last.strip
      (width, height) = size_result.split('x').map{|px| px.to_i}
      ratio = width.to_f / height.to_f
      density = `adb shell wm density |grep "Physical"`.split(": ").last.strip.to_i
      
      # override from device specs
      specs = device_presets[args[:device].to_sym]
      if not specs.nil?
        width = specs[:width]
        height = specs[:height]
        density = specs[:density]
      end
      
      # update device
      system "adb shell wm size #{width}x#{height}"
      system "adb shell wm density #{density}"
      system "adb shell stop && adb shell start"
    end

  end
  
end

task :default => ["setup:app"]