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
