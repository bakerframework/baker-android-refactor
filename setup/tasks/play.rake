# setup tasks
namespace :play do
  
  task :connect do
    if not @config["magazine"]["google_play_license_key"]
      abort "-- error: did you forget to set a google_play_license_key?"
    end
    @api = GoogleApi::Handler.new(@config["magazine"]["google_play_license_key"], File.open('setup/config/gcapi.p12').read)
  end
  
  task :update_listing => [:connect] do
    
    # create edit
    edit = @api.execute(@api.publisher.edits.insert, {packageName: package_name})
    
    # update listing
    listing = GoogleApi::Resource::PlayListing.new(@api, @config["magazine"]["app_id"]).load(edit["id"])
    listing.title = @config["magazine"]["title"]
    listing.full_description = truncate_string(@config["properties"]["dbm_basic_aso_description"], 4000)
    listing.short_description = truncate_string(@config["properties"]["dbm_basic_aso_description"], 80)
    listing.save(edit["id"])
  end
  
  task :upload_images => [:connect] do
    
    # prepare package name
    package_name = @config["magazine"]["app_id"]
    
    # create an edit
    @api.play_edit(package_name) do |edit_id|
      
      # app icon
      puts "-- uploading app icon"
      icon_image = download_cdn_image(@config["properties"]["asset_large_app_icon"])
      icon_image = create_app_icon(icon_image, 512, 512, 64, 192, 8)
      GoogleApi::Resource::PlayImage.new(@api, :icon, package_name).upload(edit_id, icon_image)
    
      # feature graphic
      puts "-- uploading feature graphic"
      feature_graphic_image = download_cdn_image(@config["properties"]["asset_feature_graphic"])
      GoogleApi::Resource::PlayImage.new(@api, :featureGraphic, package_name).upload(edit_id, feature_graphic_image)
      
      # feature graphic
      puts "-- uploading promo graphic"
      promo_graphic_image = feature_graphic_image.resize_to_fill()
      GoogleApi::Resource::PlayImage.new(@api, :promo_graphic, package_name).upload(edit_id, feature_graphic_image)
      
    end
    
  end
  
end
