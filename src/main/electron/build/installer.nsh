!macro customInstall
  #${ifNot} ${isUpdated}
      File /oname=$PLUGINSDIR\ftrDriverSetupUMDF_win8_1594.exe "${BUILD_RESOURCES_DIR}\ftrDriverSetupUMDF_win8_1594.exe"
      ExecWait '"msiexec" /i "$PLUGINSDIR\ftrDriverSetupUMDF_win8_1594.exe" /passive'
      #File /oname=$PLUGINSDIR\setup.exe "${BUILD_RESOURCES_DIR}\setup.exe"
      #ExecWait '"msiexec" /i "$PLUGINSDIR\setup.exe" /passive'
  #${endIf}
!macroend