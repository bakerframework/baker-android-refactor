module GoogleApi
  module Resource
    
    class PlayImage
      attr_accessor :image_type, :package_name, :language
  
      def initialize(api, image_type, package_name, language="en-US")
        @api = api
        self.image_type = image_type.to_s
        self.package_name = package_name
        self.language = language
      end
      
      def upload(edit_id, image, filetype=:png)
        
        # create temporary image
        tmp_file = Tempfile.new('tmp_img')
        image.format = filetype.to_s
        image.write("#{filetype}:#{tmp_file.path}")
    
        # upload
        @api.execute_request(Google::APIClient::Request.new({
          api_method: @api.publisher.edits.images.upload,
          parameters: {
            editId: edit_id,
            packageName: self.package_name,
            language: self.language,
            imageType: self.image_type,
            uploadType: 'media'
          },
          media: Google::APIClient::UploadIO.new(tmp_file.path, get_mimetype(image)),
        }))
    
        # delete temporary image
        tmp_file.delete

      end
      
      def get_mimetype(image)
        {
          png: "image/png",
          jpg: "image/jpeg",
          jpeg: "image/jpeg",
          gif: "image/gif"
        }[image.format.downcase.to_sym]
      end
      
    end
    
  end
end
