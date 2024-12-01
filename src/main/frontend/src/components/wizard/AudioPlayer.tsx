interface AudioPlayerProps {
    audioUrl: string;
}

export function AudioPlayer({ audioUrl }: AudioPlayerProps) {
    return (
        <div className="w-full">
            <audio 
                controls 
                className="w-full"
                src={audioUrl}
            >
                Your browser does not support the audio element.
            </audio>
        </div>
    );
}
