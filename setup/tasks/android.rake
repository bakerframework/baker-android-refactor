# setup tasks
namespace :android do
  
  desc "Setup the android app from magloft portal data"
  task :setup do
    
    magazine = @config["magazine"]
    
    # generate friendly app id
    package_name = magazine["app_id"].gsub(/\-/, '')
    
    # android manifest
    xml_file_inject("baker/src/main/AndroidManifest.xml", "gcm_category_name", "<category android:name=\"#{package_name}\" />")
    xml_file_inject("baker/src/main/AndroidManifest.xml", "permission_c2d", "<permission android:name=\"#{package_name}.permission.C2D_MESSAGE\" android:protectionLevel=\"signature\" />")
    xml_file_inject("baker/src/main/AndroidManifest.xml", "uses_permission_c2d", "<uses-permission android:name=\"#{package_name}.permission.C2D_MESSAGE\" />")    
    
    # build.gradle
    gradle_property("gradle.properties", "application-id", package_name)
    
    # strings
    xml_file_inject("baker/src/main/res/values/strings.xml", "app_id", "<string name=\"app_id\">#{magazine["app_id"]}</string>")
    xml_file_inject("baker/src/main/res/values/strings.xml", "app_name", "<string name=\"app_name\">#{magazine["title"]}</string>")
    xml_file_inject("baker/src/main/res/values/strings.xml", "parse_application_id", "<string name=\"parse_application_id\">#{magazine["parse_application_id"]}</string>")
    xml_file_inject("baker/src/main/res/values/strings.xml", "parse_client_key", "<string name=\"parse_client_key\">#{magazine["parse_client_key"]}</string>")
    xml_file_inject("baker/src/main/res/values/strings.xml", "google_analytics_tracking_id", "<string name=\"google_analytics_tracking_id\">#{@config["properties"]["google_tracking_code"]}</string>")
    subscriptions = []
    subscriptions.push("<item>#{package_name}.sub.#{@config["properties"]["dbm_subscription_duration"]}</item>")  if @config["properties"]["dbm_subscription_price"] != "0"
    if not @config["properties"]["dbm_subscription_duration_2"].nil? and 
       not @config["properties"]["dbm_subscription_price_2"].nil? and 
       not @config["properties"]["dbm_subscription_trial_period_2"].nil? and 
       not @config["properties"]["dbm_subscription_opt_in_offer_2"].nil?
      subscriptions.push("<item>#{package_name}.sub.#{@config["properties"]["dbm_subscription_duration_2"]}</item>")  if @config["properties"]["dbm_subscription_price_2"] != "0"
    end
    
    xml_file_inject("baker/src/main/res/values/strings.xml", "google_play_subscription_ids", "<string-array name=\"google_play_subscription_ids\">#{subscriptions.join("")}</string-array>")
    
    if @config["properties"]["shelf_background_type"] == "asset_shelf_background_pattern"
      place_cdn_image(@config["properties"]["asset_shelf_background_pattern"], [
        {path: "baker/src/main/assets/img/shelf-bg.png"}
      ])
      css_file_inject("baker/src/main/assets/background.html", "shelf_background", "background: url(img/shelf-bg.png) center center repeat;")
    elsif @config["properties"]["shelf_background_type"] == "shelf_background_gradien_color"
      css_file_inject("baker/src/main/assets/background.html", "shelf_background", "background: linear-gradient(#{@config["properties"]["shelf_background_gradient_color_1"]}, #{@config["properties"]["shelf_background_gradient_color_2"]});")
    else
      css_file_inject("baker/src/main/assets/background.html", "shelf_background", "background: #{@config["properties"]["shelf_background_solid_color"]};")
    end
    
    # App Logo
    place_cdn_image(@config["properties"]["asset_large_app_icon"], [
      {path: "baker/src/main/assets/img/logo.png", width: 1024, height: 1024},
      {path: "baker/src/main/res/drawable-xxhdpi/logo.png", width: 648, height: 648},
      {path: "baker/src/main/res/drawable-xhdpi/logo.png", width: 432, height: 432},
      {path: "baker/src/main/res/drawable-hdpi/logo.png", width: 324, height: 324},
      {path: "baker/src/main/res/drawable-mdpi/logo.png", width: 216, height: 216}
    ])
    
    # App Icons / Launcher
    place_cdn_image(@config["properties"]["asset_large_app_icon"], [
      {path: "baker/src/main/res/drawable-xxhdpi/ic_launcher.png", width: 144, height: 144, transform: :icon, radius: 8, shadow: 2, padding: 8},
      {path: "baker/src/main/res/drawable-xhdpi/ic_launcher.png", width: 96, height: 96},
      {path: "baker/src/main/res/drawable-hdpi/ic_launcher.png", width: 72, height: 72},
      {path: "baker/src/main/res/drawable-mdpi/ic_launcher.png", width: 48, height: 48}
    ])
    
    # Info dialog / Feature Graphic
    place_cdn_image(@config["properties"]["asset_feature_graphic"], [
      {path: "baker/src/main/assets/img/splash.png", width: 1024, height: 500}
    ])

    # Header images
    if @config["properties"]["shelf_header_type"] == "asset_shelf_header_full_sized_image"
      place_cdn_image(@config["properties"]["asset_shelf_header_full_sized_image"], [
        {path: "baker/src/main/assets/img/header.png", width: 2048, height: 430}
      ])
      css_file_inject("baker/src/main/assets/header.html", "header_background_size", "cover")
    else
      place_cdn_image(@config["properties"]["asset_shelf_header_centered_logo"], [
        {path: "baker/src/main/assets/img/header.png", width: 1024, height: 1024}
      ])
      css_file_inject("baker/src/main/assets/header.html", "header_background_size", "contain")
    end
    
  end
  
  desc "Reset app setup from magloft portal"
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
    
    # html
    css_file_inject("baker/src/main/assets/background.html", "shelf_background", "background: #FFF;")
    css_file_inject("baker/src/main/assets/header.html", "header_background_size", "contain")
    
    # delete images
    FileUtils.rm("baker/src/main/assets/img/header.png") if File.exists?("baker/src/main/assets/img/header.png")
    FileUtils.rm("baker/src/main/assets/img/shelf-bg.png") if File.exists?("baker/src/main/assets/img/shelf-bg.png")
    
    # restore stock images
    FileUtils.cp "setup/fixtures/baker/src/main/assets/img/logo.png", "baker/src/main/assets/img/logo.png"
    FileUtils.cp "setup/fixtures/baker/src/main/assets/img/splash.png", "baker/src/main/assets/img/splash.png"
    FileUtils.cp "setup/fixtures/baker/src/main/res/drawable-hdpi/ic_launcher.png", "baker/src/main/res/drawable-hdpi/ic_launcher.png"
    FileUtils.cp "setup/fixtures/baker/src/main/res/drawable-hdpi/logo.png", "baker/src/main/res/drawable-hdpi/logo.png"
    FileUtils.cp "setup/fixtures/baker/src/main/res/drawable-mdpi/ic_launcher.png", "baker/src/main/res/drawable-mdpi/ic_launcher.png"
    FileUtils.cp "setup/fixtures/baker/src/main/res/drawable-mdpi/logo.png", "baker/src/main/res/drawable-mdpi/logo.png"
    FileUtils.cp "setup/fixtures/baker/src/main/res/drawable-xhdpi/ic_launcher.png", "baker/src/main/res/drawable-xhdpi/ic_launcher.png"
    FileUtils.cp "setup/fixtures/baker/src/main/res/drawable-xhdpi/logo.png", "baker/src/main/res/drawable-xhdpi/logo.png"
    FileUtils.cp "setup/fixtures/baker/src/main/res/drawable-xxhdpi/ic_launcher.png", "baker/src/main/res/drawable-xxhdpi/ic_launcher.png"
    FileUtils.cp "setup/fixtures/baker/src/main/res/drawable-xxhdpi/logo.png", "baker/src/main/res/drawable-xxhdpi/logo.png"
  end
  
end
