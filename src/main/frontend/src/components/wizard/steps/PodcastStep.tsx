
import { useState, useEffect } from 'react';
import { PodcastGenerationWebSocket } from '../../../utils/websocket';
import { AudioPlayer } from '../../AudioPlayer';

interface PodcastStepProps {
    podcastId: string | null;
    onBack: () => void;
    onComplete: () => void;
}

interface GenerationState {
    status: string;
    progress: number;
    message: string | null;
    audioUrl?: string;
}

export function PodcastStep({ podcastId, onBack, onComplete }: PodcastStepProps) {
    const [generationState, setGenerationState] = useState<GenerationState>({
        status: '',
        progress: 0,
        message: null
    });
    const [error, setError] = useState<string | null>(null);
    const [consoleMessages, setConsoleMessages] = useState<string[]>([]);
    const [isCancelling, setIsCancelling] = useState(false);

    const handleCancel = async () => {
        setIsCancelling(true);
        try {
            await fetch(`/api/podcasts/${podcastId}/generate/cancel`, {
                method: 'POST'
            });
            onBack();
        } catch (error) {
            setError('Failed to cancel generation');
            setIsCancelling(false);
        }
    };

    const handleContinueInBackground = () => {
        onComplete();
    };

    const handleRegenerate = async () => {
        setConsoleMessages([]);
        setGenerationState({
            status: '',
            progress: 0,
            message: null
        });
        startGeneration();
    };

    const startGeneration = async () => {
        if (!podcastId) {
            setError('No podcast ID found');
            return null;
        }

        try {
            const response = await fetch(`/api/podcasts/${podcastId}/generate`, {
                method: 'POST'
            });

            if (!response.ok) {
                throw new Error('Failed to start podcast generation');
            }

            const ws = new PodcastGenerationWebSocket(podcastId);
            
            ws.onMessage((data) => {
                setGenerationState({
                    status: data.status,
                    progress: data.progress,
                    message: data.message,
                    audioUrl: data.audioUrl
                });
                
                setConsoleMessages(prev => [...prev, `[${data.status}] ${data.message}`]);
                
                if (data.status === 'COMPLETED') {
                    onComplete();
                } else if (data.status === 'ERROR') {
                    setError(data.message || 'An error occurred during generation');
                }
            });

            return ws;

        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to start generation');
            return null;
        }
    };

    useEffect(() => {
        let ws: PodcastGenerationWebSocket | null = null;

        const initializeGeneration = async () => {
            ws = await startGeneration();
        };

        initializeGeneration();

        return () => {
            if (ws) {
                ws.close();
            }
        };
    }, [podcastId, onComplete]);

    return (
        <div className="p-6">
            <div className="max-w-2xl mx-auto">
                <h2 className="text-2xl font-bold mb-4">Generating Podcast</h2>
                
                <div className="mb-6">
                    <div className="bg-black text-green-400 font-mono p-4 rounded-lg h-64 overflow-y-auto">
                        {consoleMessages.map((message, index) => (
                            <div key={index} className="whitespace-pre-wrap">
                                {`> ${message}`}
                            </div>
                        ))}
                    </div>
                </div>

                {!error && (
                    <div className="relative h-8 bg-gray-200 rounded-full mb-6">
                        <div 
                            className="h-full bg-primary rounded-full transition-all duration-500"
                            style={{ width: `${generationState.progress}%` }}
                        />
                        <div className="absolute inset-0 flex items-center justify-center text-sm font-medium">
                            {`${Math.round(generationState.progress)}%`}
                        </div>
                    </div>
                )}

                {error && (
                    <div className="bg-red-50 text-red-500 p-4 rounded-lg mb-6">
                        {error}
                    </div>
                )}

                {generationState.status === 'COMPLETED' && generationState.audioUrl && (
                    <div className="mb-6">
                        <AudioPlayer audioUrl={generationState.audioUrl} />
                    </div>
                )}

                <div className="flex justify-between">
                    <button
                        onClick={onBack}
                        className="px-4 py-2 border rounded hover:bg-gray-50"
                    >
                        Back
                    </button>

                    <div className="flex gap-4">
                        {generationState.status === 'COMPLETED' ? (
                            <button
                                onClick={handleRegenerate}
                                className="px-4 py-2 bg-primary text-white rounded hover:bg-primary/90"
                            >
                                Regenerate
                            </button>
                        ) : (
                            <>
                                <button
                                    onClick={handleCancel}
                                    disabled={isCancelling}
                                    className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600 disabled:opacity-50"
                                >
                                    {isCancelling ? 'Cancelling...' : 'Cancel'}
                                </button>
                                <button
                                    onClick={handleContinueInBackground}
                                    className="px-4 py-2 bg-gray-500 text-white rounded hover:bg-gray-600"
                                >
                                    Continue in Background
                                </button>
                            </>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}
