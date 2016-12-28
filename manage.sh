#!/usr/bin/env bash

function release() {
read -p "This will reset your current working tree to origin/develop, is this okay? " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]
then
    git remote update
    git pull --all
    git reset --hard origin/develop

    echo "Creating the release branch"
    mvn jgitflow:release-start -DpushReleases=true -DautoVersionSubmodules=true

    echo "Merging the release branch into develop & master, pushing changes, and tagging new version off of master"
    mvn jgitflow:release-finish -DnoReleaseBuild=true -DpushReleases=true -DnoDeploy=true

    echo "Checking out latest version of master."
    git remote update
    git pull --all
    git checkout origin/master

    echo "Deploying new release artifacts to sonatype repository."
    mvn clean deploy -P release
fi
}


function snapshot() {
read -p "This will reset your current working tree to origin/develop, is this okay? " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]
then
    git remote update
    git pull --all
    git reset --hard origin/develop

    echo "Deploying new snapshot artifacts to sonatype repository."
    mvn clean deploy -P release
fi
}

function test() {
    mvn clean test
}

case "$1" in
    snapshot)
        echo -n "Preparing to release a new snapshot version..."
        snapshot
        echo ""
    ;;
    release)
        echo -n "Preparing to release a new version..."
        release
        echo ""
    ;;
    test)
        echo -n "Running tests..."
        test
        echo ""
    ;;
    *)
        echo "Usage: ./manage.sh release|snapshot|test"
        exit 1
esac