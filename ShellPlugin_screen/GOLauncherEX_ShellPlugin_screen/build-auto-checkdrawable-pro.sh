function checkDrawable()
{
	srcFile=$1
	destFile=${srcFile##*/}
	destFileAbsoluteName="./res/drawable-mdpi/""${destFile}"
	info=$(identify $1 | cut -d " " -f 3)
	width=${info%%x*}
	height=${info#*x}
	if [ ${width} -le $2 ]; then
		if [ ! -f ${destFileAbsoluteName} ]; then 
#			echo "has copyed done"
#		else
			cp -f $1 ${destFileAbsoluteName}
			cp -f $1 "./backup/"${destFile}
			echo "copy done to drawable-mdpi for width" ${width} "in" $1
		fi
		
	elif [ ${height} -le $2 ]; then
		if [ ! -f ${destFileAbsoluteName} ]; then 
#			echo "has copyed done"
#		else
			cp -f $1 ${destFileAbsoluteName}
			cp -f $1 "./backup/"${destFile}
			echo "copy done to drawable-mdpi for height" ${height} "in" $1
		fi
	fi;
}

function checkAllDrawable()
{
	dire="./backup/"
	rm -rf "$dire"
	mkdir "$dire"

	echo "======checking drawable-xhdpi..."	
	for file in ./res/drawable-xhdpi/*
	do
	checkDrawable $file 5
	done

	echo "======checking drawable-hdpi..."	
	for file in ./res/drawable-hdpi/*
	do
	checkDrawable $file 2
	done
	
}

echo "======checking drawable in" ${PROJECT_SHELLPLUGIN} "..."
checkAllDrawable
echo "======checked done in" ${PROJECT_SHELLPLUGIN} "..."

