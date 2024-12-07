interface AudioPlayerProps {
    audioUrl: string;
}

export function AudioPlayer({ audioUrl }: AudioPlayerProps) {
    return (
        <div className="w-full bg-gray-50 rounded-md p-2 mt-2">
            <audio 
                controls 
                className="w-full h-10"
                src={audioUrl}
                preload="metadata"
            >
                <source src={audioUrl} type="audio/mpeg" />
                Your browser does not support the audio element.
            </audio>
        </div>
    );
}
