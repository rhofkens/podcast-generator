import { useState } from 'react';
import type { SyntheticEvent } from 'react';

interface AudioPlayerProps {
    audioUrl: string;
    podcastId: string | number;
}

export function AudioPlayer({ audioUrl, podcastId }: AudioPlayerProps) {
    const [error, setError] = useState(false);
    const [validatedUrl, setValidatedUrl] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const validateAudio = async () => {
            try {
                const response = await fetch(`/api/podcasts/validate-audio/${podcastId}`);
                if (!response.ok) {
                    throw new Error('Failed to validate audio URL');
                }
                const data = await response.json();
                if (data.valid) {
                    setValidatedUrl(data.url);
                } else {
                    setError(true);
                }
            } catch (err) {
                console.error('Error validating audio:', err);
                setError(true);
            } finally {
                setIsLoading(false);
            }
        };

        validateAudio();
    }, [podcastId]);

    const handleError = (e: SyntheticEvent<HTMLAudioElement, Event>) => {
        console.error('Audio player error:', e);
        const audioElement = e.currentTarget;
        console.error('Audio element error details:', {
            error: audioElement.error,
            networkState: audioElement.networkState,
            readyState: audioElement.readyState
        });
        setError(true);
    };

    if (isLoading) {
        return (
            <div className="w-full bg-gray-50 rounded-md p-4 mt-2">
                <div className="animate-pulse flex justify-center">
                    Loading audio...
                </div>
            </div>
        );
    }

    if (error || !validatedUrl) {
        return (
            <div className="w-full bg-red-50 text-red-500 rounded-md p-4 mt-2">
                Audio not available yet. Please try again later.
            </div>
        );
    }

    return (
        <div className="w-full bg-gray-50 rounded-md p-2 mt-2">
            <audio 
                controls 
                className="w-full h-10"
                src={validatedUrl}
                preload="metadata"
                onError={handleError}
            >
                <source src={validatedUrl} type="audio/mpeg" />
                Your browser does not support the audio element.
            </audio>
        </div>
    );
}
