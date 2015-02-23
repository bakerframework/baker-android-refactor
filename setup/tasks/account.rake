# setup tasks
namespace :account do

  desc "Login to magloft portal"
  task :login, [:access_token] do |_, args|
    args.with_defaults(:access_token => false)
    account_login(args[:access_token])
    puts "-- successfully logged in and synchronized with #{@config["user"]["email"]} > #{@config["magazine"]["app_id"]}"
  end
  
  desc "Fetch latest data from magloft portal"
  task :sync do
    account_login(@config["user"]["access_token"], @config["magazine"]["app_id"])
    puts "-- successfully synchronized with #{@config["user"]["email"]} > #{@config["magazine"]["app_id"]}"
  end
  
  desc "Get information about current setup"
  task :info do
    puts "user:"
    puts "-- access_token: #{@config["user"]["access_token"]}"
    puts "-- id: #{@config["user"]["id"]}"
    puts "-- firstname: #{@config["user"]["firstname"]}"
    puts "-- lastname: #{@config["user"]["lastname"]}"
    puts "-- email: #{@config["user"]["email"]}"
    puts "magazine:"
    puts "-- id: #{@config["magazine"]["id"]}"
    puts "-- title: #{@config["magazine"]["title"]}"
    puts "-- app_id: #{@config["magazine"]["app_id"]}"
    puts "-- itunes_shared_secret: #{@config["magazine"]["itunes_shared_secret"]}"
    puts "-- google_play_license_key: #{@config["magazine"]["google_play_license_key"]}"
    puts "-- parse_application_id: #{@config["magazine"]["parse_application_id"]}"
    puts "-- parse_rest_api_key: #{@config["magazine"]["parse_rest_api_key"]}"
    puts "-- parse_master_key: #{@config["magazine"]["parse_master_key"]}"
    puts "-- parse_client_key: #{@config["magazine"]["parse_client_key"]}"
    puts "-- publishing_mode: #{@config["magazine"]["publishing_mode"]}"
    puts "-- screenshot_status: #{@config["magazine"]["screenshot_status"]}"
    puts "-- development_mode: #{@config["magazine"]["development_mode"]}"
    puts "-- is_active: #{@config["magazine"]["is_active"]}"
  end
  
end