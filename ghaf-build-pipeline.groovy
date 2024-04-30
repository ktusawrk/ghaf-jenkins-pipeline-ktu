#!/usr/bin/env groovy

// SPDX-FileCopyrightText: 2024 Technology Innovation Institute (TII)
//
// SPDX-License-Identifier: Apache-2.0

pipeline {
  agent any
  parameters {
    string name: 'URL', defaultValue: 'https://github.com/tiiuae/FMO-OS.git'
    string name: 'BRANCH', defaultValue: 'main'
  }
//  Disable polling  
//  triggers {
//    pollSCM '* * * * *'
//  }
  options {
    timestamps()
    disableConcurrentBuilds()
    buildDiscarder logRotator(
      artifactDaysToKeepStr: '7',
      artifactNumToKeepStr: '10',
      daysToKeepStr: '70',
      numToKeepStr: '100'
    )
  }
  stages {
    stage('Checkout') {
      steps {
        dir('fmo') {
          checkout scmGit(
            branches: [[name: params.BRANCH]],
            extensions: [cleanBeforeCheckout()],
            userRemoteConfigs: [[url: params.URL]]
          )
        }
      }
    }
    stage('Build on x86_64') {
      steps {
        dir('fmo') {
          sh 'nix build --accept-flake-config github:tiiuae/FMO-OS#fmo-os-rugged-laptop-7330-public-debug -o result-fmo-os-rugged-laptop-7330-public-debug'
          sh 'nix build --accept-flake-config github:tiiuae/FMO-OS#fmo-os-rugged-laptop-7330-public-release -o result-fmo-os-rugged-laptop-7330-public-release'
          sh 'nix build --accept-flake-config github:tiiuae/FMO-OS#fmo-os-rugged-tablet-7230-public-debug -o result-fmo-os-rugged-tablet-7230-public-debug'
          sh 'nix build --accept-flake-config github:tiiuae/FMO-OS#fmo-os-rugged-tablet-7230-public-release -o result-fmo-os-rugged-tablet-7230-public-release'
          sh 'nix build --accept-flake-config github:tiiuae/FMO-OS#fmo-os-installer-public-debug -o result-fmo-os-installer-public-debug'
          sh 'nix build --accept-flake-config github:tiiuae/FMO-OS#fmo-os-installer-public-release -o result-fmo-os-installer-public-release'
        }
      }
    }
    stage('SBOM') {
      steps {
        dir('fmo') {
          sh 'nix run github:tiiuae/sbomnix/a1f0f88d719687acedd989899ecd7fafab42394c#sbomnix -- .#fmo-os-rugged-laptop-7330-public-debug --csv result-fmo-os-rugged-laptop-7330-public-debug.csv --cdx result-fmo-os-rugged-laptop-7330-public-debug.cdx.json --spdx result-fmo-os-rugged-laptop-7330-public-debug.spdx.json'
          sh 'nix run github:tiiuae/sbomnix/a1f0f88d719687acedd989899ecd7fafab42394c#sbomnix -- .#fmo-os-rugged-laptop-7330-public-release --csv result-fmo-os-rugged-laptop-7330-public-release.csv --cdx result-fmo-os-rugged-laptop-7330-public-release.cdx.json --spdx result-fmo-os-rugged-laptop-7330-public-release.spdx.json'
          sh 'nix run github:tiiuae/sbomnix/a1f0f88d719687acedd989899ecd7fafab42394c#sbomnix -- .#fmo-os-rugged-tablet-7230-public-debug --csv result-fmo-os-rugged-tablet-7230-public-debug.csv --cdx result-fmo-os-rugged-tablet-7230-public-debug.cdx.json --spdx result-fmo-os-rugged-tablet-7230-public-debug.spdx.json'
          sh 'nix run github:tiiuae/sbomnix/a1f0f88d719687acedd989899ecd7fafab42394c#sbomnix -- .#fmo-os-rugged-tablet-7230-public-release --csv result-fmo-os-rugged-tablet-7230-public-release.csv --cdx result-fmo-os-rugged-tablet-7230-public-release.cdx.json --spdx result-fmo-os-rugged-tablet-7230-public-release.spdx.json'
          sh 'nix run github:tiiuae/sbomnix/a1f0f88d719687acedd989899ecd7fafab42394c#sbomnix -- .#fmo-os-installer-public-debug --csv result-fmo-os-installer-public-debug.csv --cdx result-fmo-os-installer-public-debug.cdx.json --spdx result-fmo-os-installer-public-debug.spdx.json'
          sh 'nix run github:tiiuae/sbomnix/a1f0f88d719687acedd989899ecd7fafab42394c#sbomnix -- .#fmo-os-installer-public-release --csv result-fmo-os-installer-public-release.csv --cdx result-fmo-os-installer-public-release.cdx.json --spdx result-fmo-os-installer-public-release.spdx.json'
        }
      }
    }
    stage('Vulnxscan runtime') {
      steps {
        dir('fmo') {
          sh 'nix run github:tiiuae/sbomnix/a1f0f88d719687acedd989899ecd7fafab42394c#vulnxscan -- .#fmo-os-rugged-laptop-7330-public-debug --out result-vulns-fmo-os-rugged-laptop-7330-public-debug.csv'
          sh 'nix run github:tiiuae/sbomnix/a1f0f88d719687acedd989899ecd7fafab42394c#vulnxscan -- .#fmo-os-rugged-laptop-7330-public-release --out result-vulns-fmo-os-rugged-laptop-7330-public-release.csv'
          sh 'nix run github:tiiuae/sbomnix/a1f0f88d719687acedd989899ecd7fafab42394c#vulnxscan -- .#fmo-os-rugged-tablet-7230-public-debug --out result-vulns-fmo-os-rugged-tablet-7230-public-debug.csv'
          sh 'nix run github:tiiuae/sbomnix/a1f0f88d719687acedd989899ecd7fafab42394c#vulnxscan -- .#fmo-os-rugged-tablet-7230-public-release --out result-vulns-fmo-os-rugged-tablet-7230-public-release.csv'
          sh 'nix run github:tiiuae/sbomnix/a1f0f88d719687acedd989899ecd7fafab42394c#vulnxscan -- .#fmo-os-installer-public-debug --out result-vulns-fmo-os-installer-public-debug.csv'
          sh 'nix run github:tiiuae/sbomnix/a1f0f88d719687acedd989899ecd7fafab42394c#vulnxscan -- .#fmo-os-installer-public-release --out result-vulns-fmo-os-installer-public-release.csv'
        }
      }
    }
  }
  post {
    always {
      archiveArtifacts allowEmptyArchive: true, artifacts: "fmo/result-*"
      archiveArtifacts allowEmptyArchive: true, artifacts: "fmo/result-*/**"
    }
  }
}
