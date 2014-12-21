require 'yaml'

task :deploy do
  
  # Set up variables
  yaml = YAML::load(File.open('secret.yml'))
  key = yaml['key']
  apk = yaml['apk']
  
  puts "-- building signed apk"
  system "./gradlew assembleRelease -Pandroid.injected.signing.store.file=#{key['file']} -Pandroid.injected.signing.store.password='#{key['store_password']}' -Pandroid.injected.signing.key.alias=#{key['alias']} -Pandroid.injected.signing.key.password='#{key['password']}'"
  
  puts "-- installing app on device"
  system "adb install -r #{apk['file']}"
  
  puts "-- start activity"
  system "adb shell am force-stop #{apk['package']}"
  system "adb shell monkey -p #{apk['package']} -c android.intent.category.LAUNCHER 1"
  
end
