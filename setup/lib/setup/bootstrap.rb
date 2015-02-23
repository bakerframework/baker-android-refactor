require 'yaml'
require 'pry'
require 'google_api'
require 'JSON'
require 'open-uri'
require 'RMagick'
require 'setup/helper'
include Setup::Helper
include Magick

Rake.add_rakelib 'setup/tasks'
if File.exists?('setup/config/config.yml')
  @config = YAML.load_file('setup/config/config.yml')
else
  @config = {}
  puts "-- no account configured"
  account_login()
end
task :default => ["info"]
