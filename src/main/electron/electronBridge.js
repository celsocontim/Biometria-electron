const { contextBridge, ipcRenderer } = require('electron/renderer');

contextBridge.exposeInMainWorld('myAPI', {
  pegaVersao: () => ipcRenderer.invoke('get-version')
})