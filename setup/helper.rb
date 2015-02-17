require 'JSON'
require 'RMagick'
require "open-uri"

module Setup
  module Helper
    API_URL = "http://www.magloft.com"

    def xml_file_inject(file, variable, value)
      puts "-- injecting #{variable} into #{file.split("/").last}"
      start_token = "<!--\\^#{variable}-->"
      end_token = "<!--\\$#{variable}-->"
      contents = File.open(file).read
      regexp = /#{start_token}(.*)#{end_token}/m
      contents.gsub!(regexp, "<!--^#{variable}-->#{value}<!--$#{variable}-->")
      File.open(file, 'w') { |file| file.write(contents) }
    end
    
    def gradle_property(file, key, value)
      contents = File.open(file).read
      regexp = /^(#{key}=.*)$/
      contents.gsub!(regexp, "#{key}=#{value}")
      File.open(file, 'w') { |file| file.write(contents) }
    end
    
    def css_file_inject(file, variable, value)
      puts "-- injecting #{variable} into #{file.split("/").last}"
      start_token = "\\/\\*\\^#{variable}\\*\\/"
      end_token = "\\/\\*\\$#{variable}\\*\\/"
      contents = File.open(file).read
      regexp = /#{start_token}(.*)#{end_token}/m
      result = regexp.match(contents)
      contents.gsub!(regexp, "/*^#{variable}*/#{value}/*$#{variable}*/")
      File.open(file, 'w') { |file| file.write(contents) }
    end
    
    def ask(label)
      print "#{label}: "
      $stdin.gets.chomp
    end
    
    def ask_choice(label, choices, return_index=false)
      sel_index = -1
      while sel_index < 0 or sel_index >= choices.length
        puts "#{label}:"
        choices.each_with_index do |choice, index|
          puts " [#{index+1}] #{choice[:value]}"
        end
        sel_index = ask("Choose number").to_i - 1
      end
      if return_index
        sel_index
      else
        choices[sel_index][:key]
      end
    end
    
    def ask_resource_choice(label, resources, key, value)
      choices = resources.map{|resource| {key: resource[key], value: resource[value]}}
      index = ask_choice(label, choices, true)
      resources[index]
    end
    
    def init_rest
      require 'rest_client'
      RestClient.add_before_execution_proc do |req, params|
        req['X-Magloft-Accesstoken'] = @access_token
      end
    end
    
    def api_get(api, resource)
      resource = RestClient::Resource.new "#{API_URL}/api/#{api}/v1/#{resource}"
      response = resource.get(:accept => 'json')
      JSON.parse(response)
    rescue Exception => e
      abort "Error accessing your magloft account: #{e.message}"
    end
    
    def download_cdn_image(key)
      image = Magick::ImageList.new  
      image.from_blob(open("http://cdn.magloft.com/#{key}").read)
      image
    end
    
    def place_cdn_image(key, targets)
      puts "-- placing asset #{key}"
      image = download_cdn_image(key)
      targets.each do |target|  
        image.resize_to_fill!(target[:width], target[:height])  if target[:width] or target[:height]
        image.write(target[:path])
      end
    end
    
  end
end
