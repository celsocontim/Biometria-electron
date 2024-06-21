const fs = require('fs');
const yaml = require('yaml');
const path = require('path');
const chokidar = require('chokidar')

console.log("lsadsaldkmsa")

const porcentagemDataStage = 20

exports.default = async buildResult => {

  // Caminho do arquivo latest.yml gerado pelo electron-builder

  const latestYmlPath = path.join(buildResult.outDir, 'latest.yml');
  fs.access(latestYmlPath, (fs.constants || fs).R_OK, err => {
      const watcher = chokidar.watch(latestYmlPath)
      watcher.on(err ? 'add' : 'change', (event, path) => {
              // await do something
              const file = fs.readFileSync(latestYmlPath, 'utf8');
              const data = yaml.parse(file);

              // Modifica o valor de stagingPercentage
              data.stagingPercentage = porcentagemDataStage;

              // Converte o objeto de volta para YAML
              const newYaml = yaml.stringify(data);

              // Escreve o arquivo latest.yml modificado
              fs.writeFileSync(latestYmlPath, newYaml, 'utf8');
              console.log('latest.yml atualizado com stagingPercentage: ' + porcentagemDataStage);

              watcher.unwatch(latestYmlPath)
              process.exit(0)
      })
  })
/*
  if (fs.existsSync(latestYmlPath)) {
    const file = fs.readFileSync(latestYmlPath, 'utf8');
    const data = yaml.parse(file);

    // Modifica o valor de stagingPercentage
    data.stagingPercentage = 10;

    // Converte o objeto de volta para YAML
    const newYaml = yaml.stringify(data);

    // Escreve o arquivo latest.yml modificado
    fs.writeFileSync(latestYmlPath, newYaml, 'utf8');
    console.log('latest.yml atualizado com stagingPercentage: 10');
  } else {
    console.error(`Arquivo ${latestYmlPath} não encontrado!`);
  }*/
};