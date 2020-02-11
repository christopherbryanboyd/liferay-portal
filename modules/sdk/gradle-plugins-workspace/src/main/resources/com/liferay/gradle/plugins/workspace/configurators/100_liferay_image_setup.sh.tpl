#!/bin/bash

function copy_configs {
	echo "[LIFERAY] Copying /home/liferay/configs/${LIFERAY_WORKSPACE_ENVIRONMENT} files:"
	echo ""

	tree --noreport /home/liferay/configs/${LIFERAY_WORKSPACE_ENVIRONMENT}

	echo ""
	echo "[LIFERAY] ... into ${LIFERAY_HOME}."

	cp -r /home/liferay/configs/${LIFERAY_WORKSPACE_ENVIRONMENT}/* ${LIFERAY_HOME}

	echo ""
}

function copy_and_remove_scripts {

	scripts_dir_exists=false
	
	scripts_dir_has_files=false
	
	SCRIPTS_DIR="/home/liferay/configs/${LIFERAY_WORKSPACE_ENVIRONMENT}/scripts"
	
	if [ -d "$SCRIPTS_DIR" ]; then
		echo "Scripts folder exists"
		scripts_dir_exists=true
		
		if [ "$(ls -A $SCRIPTS_DIR)" ]; then
			scripts_dir_has_files=true
			
			echo "[LIFERAY] Copying $SCRIPTS_DIR files:"
			echo ""
			
			tree --noreport "$SCRIPTS_DIR"
		else
			echo "Scripts folder does not have files"
		fi
	else
		echo "Scripts folder does not exist"
	fi

	mkdir -p ${LIFERAY_MOUNT_DIR}/scripts

	if [ "$scripts_dir_exists" == true ] && [ "$scripts_dir_has_files" == true ]; then


		echo ""
		echo "[LIFERAY] ... into ${LIFERAY_MOUNT_DIR}/scripts"
	
		cp -r "$SCRIPTS_DIR"/* ${LIFERAY_MOUNT_DIR}/scripts

	fi

	if [ "$scripts_dir_exists" == true ]; then

		rm -rf "$SCRIPTS_DIR"

	fi

	echo ""
}

function main {
	copy_and_remove_scripts

	copy_configs
}

main