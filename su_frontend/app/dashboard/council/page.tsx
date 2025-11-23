'use client';

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { GraduationCap, Pencil, UserPlus, Loader2 } from 'lucide-react';
import { fetchUserCouncils, joinCouncilByCode } from '@/lib/api/council';
import { fetchUserPermissions } from '@/lib/api/permissions';
import { useCurrentUser } from '@/lib/hooks/useCurrentUser';
import { CouncilResponseDto } from '@/types/council.types';
import CouncilCard from '@/components/council/CouncilCard';
import CreateCouncilForm from '@/components/council/CreateCouncilForm';

type Tab = 'create' | 'join' | null;

export default function CouncilPage() {
  const [activeTab, setActiveTab] = useState<Tab>(null);
  const [joinCode, setJoinCode] = useState('');
  const queryClient = useQueryClient();
  const { data: user } = useCurrentUser();

  const {
    data: councils,
    isLoading: councilsLoading,
    error: councilsError,
  } = useQuery<CouncilResponseDto[]>({
    queryKey: ['userCouncils'],
    queryFn: fetchUserCouncils,
    retry: false,
  });

  const { data: permissions } = useQuery({
    queryKey: ['userPermissions'],
    queryFn: fetchUserPermissions,
  });

  const hasCreatePermission =
    permissions?.permissions?.includes('council.create') ?? false;

  const joinMutation = useMutation({
    mutationFn: joinCouncilByCode,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['userCouncils'] });
      setJoinCode('');
      setActiveTab(null);
    },
  });

  const handleJoinSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (joinCode.trim()) {
      joinMutation.mutate(joinCode.trim());
    }
  };

  if (councilsLoading) {
    return (
      <div className="flex justify-center items-center min-h-[50vh]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  const hasNoCouncils = councilsError || !councils || councils.length === 0;

  if (hasNoCouncils) {
    const firstName = user?.fullName?.split(' ')[0] || '';

    return (
      <div className="min-h-screen flex flex-col items-center justify-center p-4 bg-background text-foreground">
        
        <div className="flex flex-col items-center text-center mb-12">
          <div className="flex items-center gap-3 mb-6">
            <GraduationCap className="text-secondary h-10 w-10" />
            <h1 className="text-xl font-bold uppercase tracking-wide text-foreground">
              Samorząd
            </h1>
          </div>

          <h2 className="text-4xl font-bold mb-4">Witaj, {firstName}!</h2>
          <p className="text-txtcolor-300 max-w-md">
            Aby rozpocząć swoją przygodę z aplikacją, musisz mieć samorząd. Co robimy?
          </p>
        </div>

        <div className="w-full max-w-4xl flex flex-col items-center">
          
          <div className="flex flex-col md:flex-row gap-6 justify-center w-full max-w-2xl mb-12">
            
            {hasCreatePermission && (
              <button
                onClick={() => setActiveTab('create')}
                className={`
                  flex-1 p-12 rounded-xl flex flex-col items-center justify-center gap-4 transition-all duration-300
                  ${activeTab === 'create' 
                    ? 'bg-secondary text-background shadow-lg scale-105'
                    : 'bg-secondarybg hover:bg-inputbg text-foreground'
                  }
                `}
              >
                <Pencil className={`h-8 w-8 ${activeTab === 'create' ? 'text-background' : 'text-secondary'}`} />
                <span className="text-lg font-semibold">
                  Stwórz nowy samorząd
                </span>
              </button>
            )}


            <button
              onClick={() => setActiveTab('join')}
              className={`
                flex-1 p-12 rounded-xl flex flex-col items-center justify-center gap-4 transition-all duration-300
                ${activeTab === 'join' 
                  ? 'bg-secondary text-background shadow-lg scale-105' 
                  : 'bg-secondarybg hover:bg-inputbg text-foreground'
                }
              `}
            >
              <UserPlus className={`h-8 w-8 ${activeTab === 'join' ? 'text-background' : 'text-secondary'}`} />
              <span className="text-lg font-semibold">
                Dołącz do samorządu
              </span>
            </button>
          </div>


          <div className="w-full max-w-lg transition-all duration-500 ease-in-out">
            
            {activeTab === 'create' && (
              <div className="animate-in fade-in slide-in-from-top-4 duration-300">
                <CreateCouncilForm 
                  onCancel={() => setActiveTab(null)} 
                  onSuccess={() => setActiveTab(null)} 
                />
              </div>
            )}

            {activeTab === 'join' && (
              <div className="w-full animate-in fade-in slide-in-from-top-4 duration-300">
                <div className="mb-6">
                    <span className="text-lg font-medium text-foreground border-b-2 border-secondary pb-1">
                        Dołącz do samorządu
                    </span>
                </div>

                <form onSubmit={handleJoinSubmit} className="space-y-6">
                  <div>
                    <label htmlFor="join-code" className="block text-xs text-txtcolor-300 mb-2">
                      Kod dołączenia
                    </label>
                    <input
                      type="text"
                      id="join-code"
                      value={joinCode}
                      onChange={(e) => setJoinCode(e.target.value)}
                      className="w-full bg-inputbg text-foreground rounded-lg px-4 py-3 focus:outline-none focus:ring-2 focus:ring-secondary transition-all placeholder-txtcolor-300/50"
                      placeholder="Wprowadź kod..."
                      required
                    />
                  </div>
                  
                  {joinMutation.error && (
                    <p className="text-error text-sm">
                      {(joinMutation.error as Error).message}
                    </p>
                  )}

                  <button
                    type="submit"
                    disabled={joinMutation.isPending}
                    className="bg-primary text-background font-semibold px-8 py-3 rounded-full hover:opacity-90 disabled:opacity-50 transition-opacity w-auto"
                  >
                    {joinMutation.isPending ? 'Dołączanie...' : 'Dołącz'}
                  </button>
                </form>
              </div>
            )}

          </div>
        </div>
      </div>
    );
  }

  const activeCouncil = councils[0];
  const otherCouncils = councils.slice(1);

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-4xl font-bold text-center mb-8 text-foreground">
        Twoje Samorządy
      </h1>
      {activeCouncil && (
        <div className="mb-12">
          <div className="max-w-4xl mx-auto">
            <CouncilCard council={activeCouncil} isActive={true} />
          </div>
        </div>
      )}
      {otherCouncils.length > 0 && (
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {otherCouncils.map((council) => (
            <CouncilCard key={council.id} council={council} isActive={false} />
          ))}
        </div>
      )}
    </div>
  );
}