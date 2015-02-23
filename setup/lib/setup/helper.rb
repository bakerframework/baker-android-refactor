module Setup
  module Helper
    API_URL = "http://www.magloft.com"

    def account_login(access_token=false, app_id=false)
      # initialize api
      init_rest()
    
      # retrieve access key
      @config["user"] ||= {}
      @config["user"]["access_token"] = access_token || ask("MagLoft Access Token")
    
      # get user info
      @config["user"].merge!(api_get(:portal, 'me'))
    
      # get magazine
      magazines = api_get(:portal, 'magazines')
      if app_id
        @config["magazine"] = magazines.select{|m| m["app_id"] == app_id}.first
      else
        @config["magazine"] = (magazines.length == 1) ? magazines[0] : ask_resource_choice("Select a magazine", magazines, 'app_id', 'title')
      end

      # save config
      @config["properties"] = api_get(:portal, "magazines/#{@config["magazine"]["id"]}/properties")
    
      # save configuration
      File.open('setup/config/config.yml', 'w') { |file| file.write(YAML.dump(@config)) }
    end

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
        req['X-Magloft-Accesstoken'] = @config["user"]["access_token"]
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
        case target[:transform]
        when :icon
          image = create_app_icon(image, target[:width], target[:height], target[:padding], target[:radius], target[:shadow])
        else
          image.resize_to_fill!(target[:width], target[:height])  if target[:width] or target[:height]
        end
        image.write(target[:path])
      end
    end
    
    def create_app_icon(image, width, height, padding, radius, shadow_size)
      
      # calculate sizes
      icon_width = width - padding * 2
      icon_height = height - padding * 2
      
      # create canvas
      canvas = Image.new(width, height) {self.background_color = 'transparent'}
      
      # create resized and transformed image
      icon_image = image.resize(icon_width, icon_height)
      icon_image.border!(0, 0, 'white')
      icon_image.alpha Magick::DeactivateAlphaChannel
      
      # apply round corner mask
      mask = Image.new(width, height) {self.background_color = 'transparent'}
      Draw.new.stroke('none').stroke_width(0).fill('white').roundrectangle(padding, padding, width - padding-1, height - padding-1, radius, radius).draw(mask)
      
      # create shadow
      shadow = mask.shadow(0, 0, shadow_size)
      shadow = shadow.colorize(1, 1, 1, "gray45")

      # compose image
      canvas.composite!(icon_image, padding, padding, Magick::AddCompositeOp)
      canvas.composite!(mask, 0, 0, Magick::CopyOpacityCompositeOp)
      canvas.composite!(shadow, -shadow_size * 2, -shadow_size, Magick::DstOverCompositeOp)
      
      canvas
    end

    def truncate_string(contents, length)
      if contents.length > (length)
        contents[0..(length-4)] + "..."
      else
        contents
      end
    end
    
  end
end
