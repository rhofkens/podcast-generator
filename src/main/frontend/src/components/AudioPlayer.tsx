import { useState } from 'react';

interface AudioPlayerProps {
    audioUrl: string;
}

export function AudioPlayer({ audioUrl }: AudioPlayerProps) {
    const [error, setError] = useState(false);

    const handleError = () => {
        setError(true);
    };

    if (error) {
        return (
            <div className="w-full bg-red-50 text-red-500 rounded-md p-4 mt-2">
                Failed to load audio. Please try again later.
            </div>
        );
    }

    return (
        <div className="w-full bg-gray-50 rounded-md p-2 mt-2">
            <audio 
                controls 
                className="w-full h-10"
                src={audioUrl}
                preload="metadata"
                onError={handleError}
            >
                <source src={audioUrl} type="audio/mpeg" />
                Your browser does not support the audio element.
            </audio>
        </div>
    );
}
